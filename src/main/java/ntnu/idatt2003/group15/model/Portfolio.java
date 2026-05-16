package ntnu.idatt2003.group15.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

/**
 * Manages a collection of stock holdings for a specific player.
 */
public class Portfolio {

  private final ObservableList<Share> shares = FXCollections.observableArrayList();

  private final ObjectBinding<BigDecimal> totalMarketValueBinding = new ObjectBinding<>() {
    {
      bind(shares);
      shares.addListener((ListChangeListener<Share>) change -> {
        while (change.next()) {
          for (Share removed : change.getRemoved()) {
            unbind(removed.stock().getPriceBinding());
          }
          for (Share added : change.getAddedSubList()) {
            bind(added.stock().getPriceBinding());
          }
        }
      });
    }

    @Override
    protected BigDecimal computeValue() {
      return computeTotalMarketValue();
    }
  };

  // Cost basis is purely structural: only invalidates on add/remove, never on price ticks.
  private final ObjectBinding<BigDecimal> investedBinding =
      Bindings.createObjectBinding(this::computeInvested, shares);

  private final ObjectBinding<BigDecimal> unrealizedPnlBinding =
      Bindings.createObjectBinding(
          () -> totalMarketValueBinding.get().subtract(investedBinding.get()),
          totalMarketValueBinding, investedBinding);

  private final ObjectBinding<BigDecimal> unrealizedPnlPercentBinding =
      Bindings.createObjectBinding(() -> {
        BigDecimal inv = investedBinding.get();
        if (inv == null || inv.signum() == 0) return BigDecimal.ZERO;
        return unrealizedPnlBinding.get()
            .divide(inv, 4, RoundingMode.HALF_UP)
            .movePointRight(2);
      }, unrealizedPnlBinding, investedBinding);

  /** Observable total market value. Updates when shares are added/removed or any stock price changes. */
  public ObservableValue<BigDecimal> getTotalMarketValueProperty() {
    return totalMarketValueBinding;
  }

  /** Observable total cost basis: sum of quantity * pricePerShare across all shares. */
  public ObservableValue<BigDecimal> getInvestedProperty() {
    return investedBinding;
  }

  /** Observable unrealized profit/loss: totalMarketValue - invested. */
  public ObservableValue<BigDecimal> getUnrealizedPnlProperty() {
    return unrealizedPnlBinding;
  }

  /** Observable unrealized P/L as a percent of invested cost basis. */
  public ObservableValue<BigDecimal> getUnrealizedPnlPercentProperty() {
    return unrealizedPnlPercentBinding;
  }

  private BigDecimal computeTotalMarketValue() {
    SaleCalculator saleCalculator = new SaleCalculator();
    BigDecimal totalValue = BigDecimal.ZERO;
    for (Share currentShare : shares) {
      totalValue = totalValue.add(saleCalculator.calculateGross(currentShare));
    }
    return totalValue;
  }

  private BigDecimal computeInvested() {
    BigDecimal total = BigDecimal.ZERO;
    for (Share currentShare : shares) {
      total = total.add(currentShare.pricePerShare().multiply(currentShare.quantity()));
    }
    return total;
  }
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
        .filter(share -> share.stock().getSymbol().equalsIgnoreCase(symbol))
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
