package mozos.financialScrapper;

import lombok.extern.log4j.Log4j2;
import mozos.financialScrapper.model.Metric;
import mozos.financialScrapper.scrappers.MetricScrapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Log4j2
@Service
public class ScrapperService {

    private final List<MetricScrapper> scrappers;

    public ScrapperService(List<MetricScrapper> scrappers) {
        this.scrappers = scrappers;
    }

    public void scrappeAllMetrics() {
        log.info("Starting to fetch all metrics...");
        for (Metric metric : Metric.values()) {
            log.info("Starting to fetch metric {}", metric.getDisplayName());
            scrappers.stream()
                    .filter(s -> s.getClass().equals(metric.getScrapperClass()))
                    .findFirst()
                    .ifPresent(scrapper -> scrapper.fetchAndStoreMetric(metric));
        }
    }
}

