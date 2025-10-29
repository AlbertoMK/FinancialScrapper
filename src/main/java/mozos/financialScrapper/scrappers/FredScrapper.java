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

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Log4j2
@Service
public class FredScrapper extends MetricScrapper {

    private static String SCRAPPER_NAME = "FRED";
    private static String LIQUIDITY_SUBRESOURCE = "M2SL";
    private static String FED_RATE_SUBRESOURCE = "FEDFUNDS";
    private static String INFLATION_INDEX_SUBRESOURCE = "CPIAUCSL";
    private static String SP500_SUBRESOURCE = "SP500";
    private static String GERMANY_10_YEAR_BONDS = "IRLTLT01DEM156N";

    private MetricStorageService metricStorageService;

    public FredScrapper(MetricStorageService metricStorageService) {
        super();
        this.metricStorageService = metricStorageService;
    }

    @Value("${fred.api-key}")
    private String fredApiKey;

    @Override
    public String getScrapperName() {
        return SCRAPPER_NAME;
    }

    @Override
    public List<MetricRecord> fetchAndStoreMetric(Metric metric) {
        String subresource = getSubresource(metric);
        Optional<MetricRecord> lastRecordOptional = metricStorageService.getLastRecord(metric.getDisplayName());

        Instant lastRecordTimestamp = lastRecordOptional.isPresent() ? lastRecordOptional.get().getTimestamp() : null;
        List<MetricRecord> result = parseRawResponse(callFredSubresource(subresource, lastRecordTimestamp));
        // Volviéndolo a poner ya que hay un fallo en la API que a veces devuelve valores posteriores al startDate indicado.
        if (lastRecordTimestamp != null) {
            result = result.stream().filter(r -> r.getTimestamp().isAfter(lastRecordTimestamp)).toList();
        }
        result.forEach(m -> m.setMetricName(metric.getDisplayName()));
        metricStorageService.saveAll(result);
        return result;
    }

    private String getSubresource(Metric metric) {
        String subresource;
        switch (metric) {
            case LIQUIDITY:
                subresource = LIQUIDITY_SUBRESOURCE;
                break;
            case FED_RATE:
                subresource = FED_RATE_SUBRESOURCE;
                break;
            case INFLATION_INDEX:
                subresource = INFLATION_INDEX_SUBRESOURCE;
                break;
            case SP500_PRICE:
                subresource = SP500_SUBRESOURCE;
                break;
            case GERMANY_LONG_TERM_BONDS:
                subresource = GERMANY_10_YEAR_BONDS;
                break;
            default:
                subresource = null;
        }
        if (subresource == null) {
            throw new RuntimeException("Metric not found");
        }
        return subresource;
    }

    private String callFredSubresource(String subresource, Instant startDate) {

        String startDateString = this.instantToYYYYMMDD(startDate);
        UriComponentsBuilder urlBuilder = UriComponentsBuilder.fromUriString("https://api.stlouisfed.org/fred/series/observations")
                .queryParam("series_id", subresource)
                .queryParam("api_key", fredApiKey)
                .queryParam("file_type", "json");

        if (startDateString != null) {
            urlBuilder.queryParam("observation_start", startDateString);
        }
        String url = urlBuilder.build().toUriString();
        return this.makeHttpCall(url);
    }

    private List<MetricRecord> parseRawResponse(String rawResponse) {
        List<MetricRecord> records = new ArrayList<>();
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(rawResponse);
            JsonNode observationsList = root.get("observations");

            if (observationsList != null && observationsList.isArray()) {
                for (JsonNode obs : observationsList) {
                    MetricRecord record = new MetricRecord();
                    record.setSource(getScrapperName());
                    record.setTimestamp(Instant.parse(obs.get("date").asText() + "T00:00:00Z"));

                    String valueStr = obs.get("value").asText();
                    double valueDouble;
                    try {
                        valueDouble = Double.parseDouble(valueStr);
                        record.setValue(valueStr.equalsIgnoreCase("null") ? null : valueDouble);
                        records.add(record);
                    } catch (NumberFormatException ex) {
                        log.debug("Wrong value format found - not storing");
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error occurred parsing response.", e);
            throw new RuntimeException("Error parsing data.", e);
        }
        return records;
    }
}
