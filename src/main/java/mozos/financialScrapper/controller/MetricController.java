package mozos.financialScrapper.controller;

import lombok.extern.log4j.Log4j2;
import mozos.financialScrapper.model.Metric;
import mozos.financialScrapper.model.MetricRecord;
import mozos.financialScrapper.persistence.MetricStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Log4j2
@RestController
@RequestMapping("prometheus")
public class MetricController {

    private MetricStorageService metricStorageService;

    @Autowired
    public MetricController(MetricStorageService metricStorageService) {
        this.metricStorageService= metricStorageService;
    }

    @GetMapping(value = "metric/{metricName} produces = MediaType.APPLICATION_JSON_VALUE")
    public List<MetricRecord> getMetric(@PathVariable String metricName) {
        Metric metric;
        try {
            metric = Metric.valueOf(metricName);
        } catch (IllegalArgumentException e) {
            log.error("Metric with name {} not found.", metricName);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Metric " + metricName + " not found");
        }
        List<MetricRecord> records = metricStorageService.getAllRecords(metric);
        log.info("Latest records for metric {} marked as pushed");
        return records;
    }
}
