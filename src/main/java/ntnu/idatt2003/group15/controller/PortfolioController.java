package ntnu.idatt2003.group15.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import ntnu.idatt2003.group15.model.player.Portfolio;
import ntnu.idatt2003.group15.model.stocks.Share;

public class PortfolioController {

  private final Portfolio portfolio;

  public PortfolioController(Portfolio portfolio) {
    this.portfolio = Objects.requireNonNull(portfolio, "Portfolio cannot be null");
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

  public boolean contains(Share inputShare) {
    return portfolio.contains(inputShare);
  }
}
