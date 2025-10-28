package mozos.financialScrapper.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;
import java.util.Map;

@Data
@Document(collection = "metrics")
public class MetricRecord {

    @Id
    private String id;
    private String metricName;
    private String source;
    private Double value;
    private Instant timestamp;
    private Map<String, Object> tags;
    private boolean pushedToPrometheus;
}
