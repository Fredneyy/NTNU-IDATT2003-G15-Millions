package ntnu.idatt2003.group15.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static ntnu.idatt2003.group15.utilities.InputValidator.isBigDecimalValuePositive;

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
      isBigDecimalValuePositive("SalesPrice", salesPrice);

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
      if (isBigDecimalValuePositive("SalesPrice", price)) {
          this.prices.add(price);
      }
  }


}
