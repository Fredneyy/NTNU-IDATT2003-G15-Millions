package ntnu.idatt2003.group15.model;

public record StandardStockPrice(
    double muDelta,
    double volatility,
    double initialChange,
    double dt) implements StockPrice {}
