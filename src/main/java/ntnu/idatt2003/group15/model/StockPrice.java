package ntnu.idatt2003.group15.model;

public interface StockPrice {
  double muDelta();
  double volatility();
  double initialChange();
  double dt();
}
