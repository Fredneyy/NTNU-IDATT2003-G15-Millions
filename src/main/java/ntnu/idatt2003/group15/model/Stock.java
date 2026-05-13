package ntnu.idatt2003.group15.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import ntnu.idatt2003.group15.model.exceptions.BlankArgumentException;
import ntnu.idatt2003.group15.utilities.InputValidator;

/**
 * Represents a tradable company stock and its recorded price history.
 */
public class Stock {
  private final StringProperty symbol;
  private final StringProperty company;
  private final ObservableList<BigDecimal> prices;
  private final ObservableList<StockSectors> categories;
  private final ObjectBinding<BigDecimal> priceBinding;

  /**
   * Constructs a new  instance ready for market operations.
   *
   * @param symbol the symbol of the stock
   * @param company the name of the stock
   * @param salesPrice the price of the stock
   */
  public Stock(String symbol, String company, BigDecimal salesPrice)
      throws NullPointerException, BlankArgumentException, IllegalArgumentException {
    Objects.requireNonNull(symbol, "Company cannot be null");
    if (symbol.isBlank()) {
      throw new BlankArgumentException("Symbol cannot be blank");
    }
    Objects.requireNonNull(company, "Company cannot be null");
    if (company.isBlank()) {
      throw new BlankArgumentException("Company cannot be blank");
    }
    if (!InputValidator.isBigDecimalValuePositive(salesPrice)) {
      throw new IllegalArgumentException("SalesPrice must be positive");
    }
    this.symbol = new SimpleStringProperty(symbol);
    this.company = new SimpleStringProperty(company);
    this.categories = FXCollections.observableArrayList();
    this.prices = FXCollections.observableArrayList();
    this.prices.add(salesPrice);
    this.priceBinding = Bindings.createObjectBinding(() -> {
      if (prices.isEmpty()) {
        return BigDecimal.ZERO;
      } else {
        return prices.getLast();
      }
    }, prices);
  }

  /**
   * Returns the observable  for UI binding and real-time updates.
   *
   * @return the symbol property of the stock
   */
  public StringProperty symbolProperty() {
    return symbol;
  }

  /**
   * Returns the observable  for UI binding and real-time updates.
   *
   * @return the string property of the company name
   */
  public StringProperty companyProperty() {
    return company;
  }

  /**
   * Updates the  to modify application state.
   *
   * @param categories the categories of the stock
   */
  public void setCategories(List<StockSectors> categories) {
    this.categories.clear();
    this.categories.addAll(categories);
  }

  /**
   * Returns the current symbol of the stock.
   *
   * @return the current symbol
   */
  public String getSymbol() {
    return symbol.get();
  }

  /**
   * Returns the company name of the stock.
   *
   * @return the company name
   */
  public String getCompany() {
    return company.get();
  }

  /**
   * Returns the current sales price.
   *
   * @return the sales price
   */
  public BigDecimal getSalesPrice() {
    return prices.getLast();
  }

  /**
   * Adds a new sales price to the stock.
   *
   * @param price the new price
   */
  public void addNewSalesPrice(BigDecimal price)
      throws NullPointerException, IllegalArgumentException {
    if (InputValidator.isBigDecimalValuePositive(price)) {
      prices.add(price);
    } else {
      throw new IllegalArgumentException("Price must be positive");
    }
  }

  /**
   * Returns the historical prices of the stock as a live observable list.
   * Listeners attached to the returned list will fire on every new price.
   *
   * @return the observable list of prices for the stock
   */
  public ObservableList<BigDecimal> getHistoricalPrices() {
    return prices;
  }

  /**
   * Returns the highest price of the stock.
   *
   * @return the highest price
   */
  public BigDecimal getHighestPrice() {
    return prices.stream()
            .max(BigDecimal::compareTo)
            .orElse(BigDecimal.ZERO);
  }

  /**
   * Returns the lowest price of the stock.
   *
   * @return the lowest price
   */
  public BigDecimal getLowestPrice() {
    return prices.stream()
        .min(BigDecimal::compareTo)
        .orElse(BigDecimal.ZERO);
  }

  /**
   * Returns the latest price change.
   *
   * @return the latest price change in amount
   */
  public BigDecimal getLatestPriceChange() {
    if (prices.size() < 2) {
      return BigDecimal.ZERO;
    }

    BigDecimal lastPrice = prices.getLast();
    BigDecimal secondLastPrice = prices.get(prices.size() - 2);

    return lastPrice.subtract(secondLastPrice);
  }

  /**
   * Returns the latest price change.
   *
   * @return the latest price change in percentage
   */
  public BigDecimal getLatestPriceChangeRelative() {
    if (prices.size() < 2) {
      return BigDecimal.ZERO;
    }

    BigDecimal change = getLatestPriceChange();
    BigDecimal oldPrice = prices.get(prices.size() - 2);

    return change.divide(oldPrice, 5, RoundingMode.HALF_UP);
  }

  /**
   * Returns the categories property.
   *
   * @return categories property
   */
  public ObservableList<StockSectors> getCategories() {
    return categories;
  }

  /**
   * Returns the latest price object binding
   * @return object binding for the latest price
   */
  public ObjectBinding<BigDecimal> getPriceBinding() {
    return priceBinding;
  }

  /**
   * Checks if two stocks are equal in name or symbol.
   *
   * @param o the stock to check if equals this
   * @return {@code true} if stock is same instance, has same name or same symbol.
   */
  @Override
  public boolean equals(Object o) {
    if (o == null) {
      return false;
    }
    if (this == o) {
      return true;
    }
    if (!(o instanceof Stock stock)) {
      return false;
    }
    return getSymbol().equalsIgnoreCase(stock.getSymbol())
        && getCompany().equalsIgnoreCase(stock.getCompany());
  }
}
