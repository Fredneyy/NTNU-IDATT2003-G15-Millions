package ntnu.idatt2003.group15.model;

public record News(
    String headline,
    String sector,
    double muDelta,
    double volatility,
    double initialChange,
    double dt) implements StockPrice {}
