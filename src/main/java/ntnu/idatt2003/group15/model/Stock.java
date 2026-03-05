package ntnu.idatt2003.group15.model;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

public class Stock {
  private final String symbol;
  private final String company;
  private List<BigDecimal> prices;

  public Stock(String symbol, String company, BigDecimal salesPrice) {
      Objects.requireNonNull(symbol, "Symbol cannot be null");
      Objects.requireNonNull(company, "Company cannot be null");
      Objects.requireNonNull(salesPrice, "SalesPrice cannot be null");
    this.symbol = symbol;
    this.company = company;
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

  public void addNewSalesPrice(BigDecimal price) {
    this.prices.add(price);
  }
}
