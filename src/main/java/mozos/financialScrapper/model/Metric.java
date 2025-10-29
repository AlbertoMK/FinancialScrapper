package mozos.financialScrapper.model;

import mozos.financialScrapper.scrappers.AlphaVantageScrapper;
import mozos.financialScrapper.scrappers.FredScrapper;
import mozos.financialScrapper.scrappers.MetricScrapper;

public enum Metric {

    LIQUIDITY("M2 Money Supply", FredScrapper.class), // Mide la liquidez en EEUU en Billions en el mercado
    FED_RATE("Federal Funds Rate", FredScrapper.class), // Mide las tasas de interés fijadas por la FED
    INFLATION_INDEX("Consumer Price Index", FredScrapper.class), // Índice que muestra el IPC de EEUU
    GERMANY_LONG_TERM_BONDS("Germany 10-year bond rates", FredScrapper.class), // Intereses provistos por los bonos alemanes a 10 años
    VIX("Volatility expectation index", FredScrapper.class), // Mide las expectativas de la volatilidad del mercado en porcentajes.
    PETROL("Crude oil prices: Brent", FredScrapper.class), // Precio en doláres por barril de petróleo.
    SENTIMENT("Consumer financial sentiment", FredScrapper.class), // Índice de sentimiento financiero por consumidores estadounidenses
    PIB("Gross Domestic Product", FredScrapper.class), // Billones de dólares del PIB  de EEUU
    GOLD_PRICE("Gold price", AlphaVantageScrapper.class), // Precio de índice de oro en usd
    SP500_PRICE("SP500 Price", AlphaVantageScrapper.class); // Precios del ínidice SP500 en $


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
