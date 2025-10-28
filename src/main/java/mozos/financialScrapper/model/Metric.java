package mozos.financialScrapper.model;

import mozos.financialScrapper.scrappers.FredScrapper;
import mozos.financialScrapper.scrappers.MetricScrapper;

public enum Metric {

    LIQUIDITY("M2 Money Supply", FredScrapper.class), // Mide la liquidez en EEUU en Billions en el mercado
    FED_RATE("Federal Funds Rate", FredScrapper.class), // Mide las tasas de interés fijadas por la FED
    INFLATION_INDEX("Consumer Price Index", FredScrapper.class); // Índice que muestra el IPC de EEUU

    private final String displayName;
    private final Class<? extends MetricScrapper> scrapperClass;

    Metric(String displayName, Class<? extends MetricScrapper> scrapperClass) {
        this.displayName = displayName;
        this.scrapperClass = scrapperClass;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Class<? extends MetricScrapper> getScrapperClass() {
        return scrapperClass;
    }
}
