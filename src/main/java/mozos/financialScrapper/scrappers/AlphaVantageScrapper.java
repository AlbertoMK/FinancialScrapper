package mozos.financialScrapper.scrappers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import mozos.financialScrapper.model.Metric;
import mozos.financialScrapper.model.MetricRecord;
import mozos.financialScrapper.persistence.MetricStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Log4j2
@Service
public class AlphaVantageScrapper extends MetricScrapper {

    @Value("${alphavantage.api-key}")
    private String alphaVantageApiKey;

    private static String SCRAPPER_NAME = "ALPHA VANTAGE";
    private static String GOLD_TICKER = "iau";
    private static String SP500_TICKER = "spy";

    private MetricStorageService metricStorageService;

    public AlphaVantageScrapper(MetricStorageService metricStorageService) {
        super();
        this.metricStorageService = metricStorageService;
    }

    @Override
    public String getScrapperName() {
        return SCRAPPER_NAME;
    }

    @Override
    public List<MetricRecord> fetchAndStoreMetric(Metric metric) {
        String ticker = getTicker(metric);
        Optional<MetricRecord> lastRecordOptional = metricStorageService.getLastRecord(metric.getDisplayName());

        Instant lastRecordTimestamp = lastRecordOptional.isPresent() ? lastRecordOptional.get().getTimestamp() : null;
        List<MetricRecord> result = parseRawResponse(callAlphaTicker(ticker));
        if (lastRecordTimestamp != null) {
            result = result.stream().filter(r -> r.getTimestamp().isAfter(lastRecordTimestamp)).toList();
        }
        result.forEach(m -> m.setMetricName(metric.getDisplayName()));
        metricStorageService.saveAll(result);
        return result;
    }

    private List<MetricRecord> parseRawResponse(String response) {
        List<MetricRecord> records = new ArrayList<>();
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response);
            JsonNode timeSeries = root.get("Time Series (Daily)");
            if (timeSeries != null) {
                Iterator<Map.Entry<String, JsonNode>> fields = timeSeries.fields();
                while (fields.hasNext()) {
                    MetricRecord record = new MetricRecord();
                    record.setSource(getScrapperName());

                    Map.Entry<String, JsonNode> entry = fields.next();
                    String dateStr = entry.getKey();
                    record.setTimestamp(Instant.parse(dateStr + "T00:00:00Z"));

                    JsonNode dailyData = entry.getValue();
                    try {
                        if (dailyData.has("4. close")) {
                            double close = dailyData.get("4. close").asDouble();
                            record.setValue(close);
                            records.add(record);
                        } else {
                            log.debug("Close data not found - skipping it");
                        }
                    } catch (NumberFormatException ex) {
                        log.debug("Number format unexpected - skipping it");
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error occurred parsing response.", e);
            throw new RuntimeException("Error parsing data.", e);
        }
        return records;
    }

    private String getTicker(Metric metric) {
        String ticker;
        switch (metric) {
            case GOLD_PRICE:
                ticker = GOLD_TICKER;
                break;
            case SP500_PRICE:
                ticker = SP500_TICKER;
                break;
            default:
                ticker = null;
        }
        if (ticker == null) {
            throw new RuntimeException("Metric not found");
        }
        return ticker;
    }

    private String callAlphaTicker(String ticker) {
        String url = UriComponentsBuilder.fromUriString("https://www.alphavantage.co/query")
                .queryParam("function", "TIME_SERIES_DAILY")
                .queryParam("outputsize", "full")
                .queryParam("symbol", ticker)
                .queryParam("apikey", alphaVantageApiKey)
                .build().toUriString();
        return this.makeHttpCall(url);
    }
}
