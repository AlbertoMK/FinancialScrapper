package mozos.financialScrapper.scrappers;

import mozos.financialScrapper.model.Metric;
import mozos.financialScrapper.model.MetricRecord;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;

public abstract class MetricScrapper {

    protected final WebClient webClient;

    protected MetricScrapper() {
        this.webClient = WebClient.builder()
                .codecs(configurer -> configurer.defaultCodecs()
                        .maxInMemorySize(16 * 1024 * 1024))
                .build();
    }


    public abstract String getScrapperName();
    public abstract List<MetricRecord> fetchAndStoreMetric(Metric metric);

    /**
     * Hace una llamada HTTP GET a la URL indicada.
     * Devuelve el cuerpo como String o lanza una RuntimeException en caso de error.
     */
    protected String makeHttpCall(String url) {
        try {
            return webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (WebClientResponseException e) {
            throw new RuntimeException("HTTP call failed with status " + e.getStatusCode() + ": " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            throw new RuntimeException("Error making HTTP call to " + url, e);
        }
    }

    /**
     * Devuelve un Instant en formato YYYY-MM-DD.
     * Si el instant es nulo, devuelve nulo.
     */
    protected String instantToYYYYMMDD(Instant instant) {
        return instant == null ? null : instant.toString().substring(0, 10);
    }
}
