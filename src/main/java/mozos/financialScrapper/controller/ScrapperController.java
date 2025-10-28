package mozos.financialScrapper.controller;

import lombok.extern.log4j.Log4j2;
import mozos.financialScrapper.ScrapperService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@RestController
public class ScrapperController {

    private final ScrapperService scrapperService;

    public ScrapperController(ScrapperService scrapperService) {
        this.scrapperService = scrapperService;
    }

    @PostMapping("/scrappe")
    public void triggerScrapping() {
        log.info("Trigger received to scrappe all metrics");
        scrapperService.scrappeAllMetrics();
    }
}

