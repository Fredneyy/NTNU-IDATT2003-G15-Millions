package ntnu.idatt2003.group15.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Stock {
  private final String symbol;
  private final String company;
  private List<BigDecimal> prices;

  public Stock(String symbol, String company, BigDecimal salesPrice) throws NullPointerException, BlankArgumentException, IllegalArgumentException {
      Objects.requireNonNull(symbol, "Company cannot be null");
      if (symbol.isBlank()) {
          throw new BlankArgumentException("Symbol cannot be blank");
      }
      Objects.requireNonNull(company, "Company cannot be null");
      if (company.isBlank()) {
          throw new BlankArgumentException("Company cannot be blank");
      }
      isSalesPriceValid(salesPrice);

    this.symbol = symbol;
    this.company = company;
    prices = new ArrayList<BigDecimal>();
    this.prices.add(salesPrice);
  }


  public String getSymbol() {
    return symbol;
  }

  public String getCompany() {
    return company;
  }

  public BigDecimal getSalesPrice() {
    return this.prices.getLast();
  }

  public void addNewSalesPrice(BigDecimal price) throws IllegalArgumentException, NullPointerException {
      if (isSalesPriceValid(price)) {
          this.prices.add(price);
      }
  }

  private boolean isSalesPriceValid (BigDecimal price) throws IllegalArgumentException, NullPointerException {
      if (price == null) {
          throw new NullPointerException("SalesPrice cannot be null");
      }

      if (price.compareTo(BigDecimal.ZERO) <= 0) {
          throw new IllegalArgumentException("SalesPrice must be greater than zero");
      }

      return true;
  }
}
