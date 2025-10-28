package mozos.financialScrapper.persistence;

import mozos.financialScrapper.model.Metric;
import mozos.financialScrapper.model.MetricRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class MetricStorageService {
    private final MetricRepository metricRepository;
    private final MongoTemplate mongoTemplate;

    @Autowired
    public MetricStorageService(MetricRepository metricRepository, MongoTemplate mongoTemplate) {
        this.metricRepository = metricRepository;
        this.mongoTemplate = mongoTemplate;
    }

    public void saveAll(List<MetricRecord> records) {
        metricRepository.saveAll(records);
    }

    public Optional<MetricRecord> getLastRecord(String metricName) {
        return Optional.ofNullable(metricRepository.findTopByMetricNameOrderByTimestampDesc(metricName));
    }

    public List<MetricRecord> getAllRecords(Metric metric) {
        return metricRepository.findByMetricName(metric.getDisplayName());
    }

    public List<MetricRecord> getRecordsAfterInstant(Metric metric, Instant instant) {
        return metricRepository.findByMetricNameAndTimestampAfter(metric.getDisplayName(), instant);
    }
}
