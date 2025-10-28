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
public class PrometheusController {

    private MetricStorageService metricStorageService;

    @Autowired
    public PrometheusController(MetricStorageService metricStorageService) {
        this.metricStorageService= metricStorageService;
    }

    @GetMapping(value = "metric/{metricName}", produces = "text/plain; version=0.0.4")
    public String getMetric(@PathVariable String metricName) {
        Metric metric;
        try {
            metric = Metric.valueOf(metricName);
        } catch (IllegalArgumentException e) {
            log.error("Metric with name {} not found.", metricName);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Metric " + metricName + " not found");
        }
        List<MetricRecord> records = metricStorageService.getUnpushedRecordsByMetric(metric);
        metricStorageService.markRecordsAsPushed(records);
        log.info("Latest records for metric {} marked as pushed");
        return parseMetricToPrometheusFormat(metric, records);
    }

    private String parseMetricToPrometheusFormat(Metric metric, List<MetricRecord> records) {
        StringBuilder sb = new StringBuilder();
        String metricNameInSnakeCase = metric.getDisplayName().toLowerCase().replace(" ", "_");
        sb
                .append("# HELP " + metricNameInSnakeCase + "\n")
                .append("# TYPE " + metricNameInSnakeCase + " gauge\n");
        records.forEach(r -> {
            if (r.getValue() != null) {
                sb.append(metricNameInSnakeCase)
                        .append(" ")
                        .append(r.getValue())
                        .append(" ")
                        .append(r.getTimestamp().toEpochMilli() / 1000)
                        .append("\n");
            }
        });
        return sb.toString();
    }
}
