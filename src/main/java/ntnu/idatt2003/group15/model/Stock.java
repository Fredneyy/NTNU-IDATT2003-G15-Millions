package ntnu.idatt2003.group15.model;

import ntnu.idatt2003.group15.model.exceptions.BlankArgumentException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static ntnu.idatt2003.group15.utilities.InputValidator.isBigDecimalValuePositive;

public class Stock {
      private final String symbol;
      private final String company;
      private List<BigDecimal> prices;
      private final List<String> categories;

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
        this.categories = new ArrayList<>();
        prices = new ArrayList<>();
        this.prices.add(salesPrice);
      }

      public void setCategories(List<String> categories) {
        this.categories.clear();
        this.categories.addAll(categories);
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

      public List<BigDecimal> getHistoricalPrices() {
          return prices;
      }

      public BigDecimal getHighestPrice() {
          return prices.stream()
                  .max(BigDecimal::compareTo)
                  .orElse(BigDecimal.ZERO);
      }

    public BigDecimal getLowestPrice() {
        return prices.stream()
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
    }

    public BigDecimal getLatestPriceChange() {
          if (prices.size() < 2) {
              return BigDecimal.ZERO;
          }

          BigDecimal lastPrice = prices.getLast();
          BigDecimal secondLastPrice = prices.get(prices.size() - 2);

          return lastPrice.subtract(secondLastPrice);
    }

    public BigDecimal getLatestPriceChangeRelative() {
        if (prices.size() < 2) return BigDecimal.ZERO;

        BigDecimal change = getLatestPriceChange();
        BigDecimal oldPrice = prices.get(prices.size() - 2);

        return change.divide(oldPrice, 5, RoundingMode.HALF_UP);
    }

    public List<String> getCategories() {
        return List.copyOf(categories);
    }
}
