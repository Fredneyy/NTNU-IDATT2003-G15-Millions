package ntnu.idatt2003.group15.model.player;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import ntnu.idatt2003.group15.model.stocks.Share;
import ntnu.idatt2003.group15.model.transactions.SaleCalculator;

/**
 * The type Portfolio.
 */
public class Portfolio {

  private final ObservableList<Share> shares = FXCollections.observableArrayList(
      share -> new Observable[]{
          share.quantityProperty(),
          share.stock().getPriceBinding()
      }
  );
  private final Map<String, Share> shareIndex = new HashMap<>();

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

  private final ObjectBinding<BigDecimal> investedBinding =
      Bindings.createObjectBinding(this::computeInvested, shares);

  private final ObjectBinding<BigDecimal> unrealizedPnlBinding =
      Bindings.createObjectBinding(
          () -> totalMarketValueBinding.get().subtract(investedBinding.get()),
          totalMarketValueBinding, investedBinding);

  private final ObjectBinding<BigDecimal> unrealizedPnlPercentBinding =
      Bindings.createObjectBinding(() -> {
        BigDecimal inv = investedBinding.get();
        if (inv == null || inv.signum() == 0) {
          return BigDecimal.ZERO;
        }
        return unrealizedPnlBinding.get()
            .divide(inv, 4, RoundingMode.HALF_UP)
            .movePointRight(2);
      }, unrealizedPnlBinding, investedBinding);

  /**
   * Gets total market value property.
   *
   * @return the total market value property
   */
  public ObservableValue<BigDecimal> getTotalMarketValueProperty() {
    return totalMarketValueBinding;
  }

  /**
   * Gets invested property.
   *
   * @return the invested property
   */
  public ObservableValue<BigDecimal> getInvestedProperty() {
    return investedBinding;
  }

  /**
   * Gets unrealized pnl property.
   *
   * @return the unrealized pnl property
   */
  public ObservableValue<BigDecimal> getUnrealizedPnlProperty() {
    return unrealizedPnlBinding;
  }

  /**
   * Gets unrealized pnl percent property.
   *
   * @return the unrealized pnl percent property
   */
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
   * @param inputShare the share to add to portfolio
   * @return {@code true} if added, {@code false} otherwise
   * @throws NullPointerException the null pointer exception
   */
  public boolean addShare(Share inputShare) throws NullPointerException {
    Objects.requireNonNull(inputShare, "Share cannot be null");
    shareIndex.put(inputShare.stock().getSymbol(), inputShare);
    return shares.add(inputShare);
  }

  /**
   * Removes a sold share from the portfolio holding.
   *
   * @param inputShare the share to remove from portfolio
   * @return {@code true} if removed, {@code false} otherwise
   * @throws NullPointerException the null pointer exception
   */
  public boolean removeShare(Share inputShare) throws NullPointerException {
    Objects.requireNonNull(inputShare, "Share cannot be null");

    boolean removed = shares.remove(inputShare);
    if (removed) {
      shareIndex.remove(inputShare.stock().getSymbol());
    }

    return removed;
  }

  /**
   * Returns the share matching the given symbol, or {@code null} if not held.
   *
   * @param symbol the stock symbol to look up
   * @return the matching {@link Share}, or {@code null}
   * @throws NullPointerException the null pointer exception
   */
  public Share getShare(String symbol) throws NullPointerException {
    Objects.requireNonNull(symbol, "Symbol cannot be null");
    return shareIndex.get(symbol);
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
   * Checks if the exact given share instance is held in this portfolio.
   *
   * @param inputShare check if share is in the portfolio
   * @return {@code true} if portfolio contains, {@code false} otherwise
   * @throws NullPointerException the null pointer exception
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