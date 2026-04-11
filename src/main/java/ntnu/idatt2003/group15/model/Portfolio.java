package ntnu.idatt2003.group15.model;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Manages a collection of stock holdings for a specific player.
 */
public class Portfolio {
  private final ObservableList<Share> shares = FXCollections.observableArrayList();

  /**
   * Adds a purchased share to the portfolio holding.
   *
   * @param inputShare the share to add to portfolia
   * @return {@code true} if added, {@code false} otherwise
   */
  public boolean addShare(Share inputShare) throws NullPointerException {
    Objects.requireNonNull(inputShare, "Share cannot be null");
    return shares.add(inputShare);
  }

  /**
   * Removes a sold share from the portfolio holding.
   *
   * @param inputShare the share to remove from portfolio
   * @return {@code true} if removed, {@code false} otherwise
   */
  public boolean removeShare(Share inputShare) throws NullPointerException {
    Objects.requireNonNull(inputShare, "Share cannot be null");
    return shares.remove(inputShare);
  }

  /**
   * Returns the observable list of shares for UI binding and real-time updates.
   *
   * @return the observable list of shares
   */
  public ObservableList<Share> getListProperty() {
    return shares;
  }

  /**
   * Returns a snapshot of the current shares held in the portfolio.
   *
   * @return an unmodifiable copy of the list of shares
   */
  public List<Share> getShares() {
    return List.copyOf(shares);
  }

  /**
   * Returns a list of held shares that match the given stock symbol.
   *
   * @param symbol the symbol to search for
   * @return a {@code List} containing shares with matching symbol
   */
  public List<Share> getShares(String symbol) throws NullPointerException {
    Objects.requireNonNull(symbol, "Symbol cannot be null");
    return shares.stream()
        .filter(share -> share.getStock().getSymbol().equalsIgnoreCase(symbol))
        .toList();
  }

  /**
   * Checks if the exact given share instance is held in this portfolio.
   *
   * @param inputShare check if share is in the portfolio
   * @return {@code true} if portfolio contains,{@code false} otherwise
   */
  public boolean contains(Share inputShare) throws NullPointerException {
    Objects.requireNonNull(inputShare, "Share cannot be null");
    return shares.contains(inputShare);
  }

  /**
   * Calculates the total current market value of all held stocks in this portfolio.
   *
   * @return the total market value of the portfolio
   */
  public BigDecimal getTotalMarketValue() {
    SaleCalculator saleCalculator = new SaleCalculator();
    BigDecimal totalValue = BigDecimal.ZERO;
    for (Share currentShare : shares) {
      totalValue = totalValue.add(saleCalculator.calculateGross(currentShare));
    }
    return totalValue;
  }
}
