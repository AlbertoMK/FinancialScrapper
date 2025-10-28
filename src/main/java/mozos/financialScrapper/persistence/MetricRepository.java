package mozos.financialScrapper.persistence;

import mozos.financialScrapper.model.MetricRecord;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.Instant;
import java.util.List;

public interface MetricRepository extends MongoRepository<MetricRecord, String> {
    List<MetricRecord> findByMetricName(String metricName);
    MetricRecord findTopByMetricNameOrderByTimestampDesc(String metricName);
    List<MetricRecord> findByMetricNameAndTimestampAfter(String metricName, Instant timestamp);
    List<MetricRecord> findByMetricNameAndPushedToPrometheusFalse(String metricName);
}
