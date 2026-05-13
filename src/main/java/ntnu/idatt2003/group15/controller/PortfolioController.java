package ntnu.idatt2003.group15.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import ntnu.idatt2003.group15.model.Portfolio;
import ntnu.idatt2003.group15.model.Share;

public class PortfolioController {

  private final Portfolio portfolio;

  public PortfolioController(Portfolio portfolio) {
    this.portfolio = Objects.requireNonNull(portfolio, "Portfolio cannot be null");
  }

  public boolean addShare(Share inputShare) {
    return portfolio.addShare(inputShare);
  }

  public boolean removeShare(Share inputShare) {
    return portfolio.removeShare(inputShare);
  }

  public ObservableList<Share> getListProperty() {
    return portfolio.getListProperty();
  }

  public List<Share> getShares() {
    return portfolio.getShares();
  }

  public ObservableValue<BigDecimal> totalMarketValueProperty() {
    return portfolio.getTotalMarketValueProperty();
  }

  public List<Share> getShares(String symbol) {
    return portfolio.getShares(symbol);
  }

  public boolean contains(Share inputShare) {
    return portfolio.contains(inputShare);
  }

  public BigDecimal getTotalMarketValue() {
    return portfolio.getTotalMarketValue();
  }
}
