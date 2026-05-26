package ntnu.idatt2003.group15.model;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableIntegerValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import ntnu.idatt2003.group15.model.exceptions.BlankArgumentException;
import ntnu.idatt2003.group15.model.factories.TransactionFactory;
import ntnu.idatt2003.group15.model.factories.TransactionType;
import ntnu.idatt2003.group15.model.news.NewsItem;
import ntnu.idatt2003.group15.model.player.Player;
import ntnu.idatt2003.group15.model.stocks.Share;
import ntnu.idatt2003.group15.model.stocks.Stock;
import ntnu.idatt2003.group15.model.transactions.Purchase;
import ntnu.idatt2003.group15.model.transactions.Sale;
import ntnu.idatt2003.group15.utilities.StockSimulator;

/**
 * Represents a stock exchange managing a collection of active stocks
 * and their weekly trading values.
 */
public class Exchange {
  /** Simulator time step: one trading week per {@link #advance()} call. */
  private static final double SIMULATOR_DT = 1.0 / 52.0;

  private final String name;
  private final IntegerProperty week = new SimpleIntegerProperty(1);
  private final Map<String, Stock> stockMap;
  private final StockSimulator simulator = new StockSimulator(SIMULATOR_DT);
  private final BigDecimal commission = new BigDecimal("0.01");
  private final BigDecimal tax = new BigDecimal("0.37");
  private double volatilityMultiplier = 1.0;
  private ObservableList<NewsItem> news = FXCollections.observableArrayList();

  /**
   * Initializes a new stock exchange with the given name and collection of initial stocks.
   *
   * @param name the name of the exchange
   * @param stocks the list containing initial stocks
   */
  public Exchange(String name, List<Stock> stocks)
      throws BlankArgumentException, NullPointerException {
    Objects.requireNonNull(name, "name cannot be null");
    if (name.isBlank()) {
      throw new BlankArgumentException(
          "The exchange name can't be empty. Please give the stock exchange a name.");
    }
    Objects.requireNonNull(stocks, "stocks cannot be null");
    this.name = name;
    this.stockMap = stocks.stream().collect(Collectors.toMap(Stock::getSymbol, stock -> stock));
  }

  /**
   * Sets the news events observable for applying changed stock drift, volatility etc.
   *
   * @param news the list to watch for events
   */
  public void setNewsObservableList(ObservableList<NewsItem> news) {
    this.news = Objects.requireNonNull(news, "news cannot be null");
  }

  /**
   * Retrieves the name of the exchange.
   *
   * @return the name of the exchange
   */
  public String getName() {
    return name;
  }

  /** Restore the simulation week (used when loading a saved game).
   * @param week the week you would like to set
   * */
  public void setWeek(int week) {
    this.week.set(week);
  }

  /**
   * Sets the volatility multiplier
   * @param volatilityMultiplier the volatility multiplier you want to set
   * */
  public void setVolatilityMultiplier(double volatilityMultiplier) {
    this.volatilityMultiplier = volatilityMultiplier;
  }

  /**
   * Gets the week property
   * @return current week int
   * */
  public ObservableIntegerValue getWeekProperty() {
    return week;
  }

  /**
   * Checks if a stock is listed on this exchange based on its symbol.
   *
   * @param symbol the symbol to search for
   * @return if the stock exists in the map
   * @throws  NullPointerException if any parameter is null
   */
  public boolean hasStock(String symbol) throws NullPointerException {
    Objects.requireNonNull(symbol, "symbol cannot be null");
    return this.stockMap.containsKey(symbol);
  }

  /**
   * Retrieves a listed stock using its unique ticker symbol.
   *
   * @param symbol the symbol to search for
   * @return the {@code Stock} in the map matching the symbol
   * @throws NullPointerException if any parameter is null
   */
  public Stock getStock(String symbol) throws NullPointerException {
    Objects.requireNonNull(symbol, "symbol cannot be null");
    return this.stockMap.get(symbol);
  }

  /**
   * Retrieves all stocks currently listed on the exchange.
   *
   * @return a {@code List} containing all listed stocks
   */
  public List<Stock> getAllStocks() {
    return List.copyOf(stockMap.values());
  }

  /**
   * Searches for listed stocks matching the specified symbol or company name substring.
   *
   * @param searchTerm the term to search for
   * @return a {@code List} containing stock matching searchTerm
   * @throws NullPointerException if any parameter is null
   */
  public List<Stock> findStocks(String searchTerm) throws NullPointerException {
    Objects.requireNonNull(searchTerm, "Search term cannot be null");
    return stockMap.entrySet().stream()
        .filter(entry ->
            entry.getKey().contains(searchTerm)
                || entry.getValue().getCompany().contains(searchTerm))
        .map(Map.Entry::getValue)
        .toList();
  }

  /**
   * Executes a purchase transaction for the given stock and assigns it to the player.
   *
   * @param symbol the symbol of the stock
   * @param quantity the amount to purchase
   * @param player the player
   * @throws NullPointerException if any parameter is null
   */
  public void buy(String symbol, BigDecimal quantity, Player player) throws NullPointerException {
    Objects.requireNonNull(symbol, "Symbol cannot be null");
    Objects.requireNonNull(quantity, "Quantity cannot be null");
    Objects.requireNonNull(player, "Player cannot be null");
    Stock stock = getStock(symbol);

    Share existing = player.getPortfolio().getShare(symbol);
    Share share = new Share(stock, quantity, stock.getSalesPrice());
    if (existing != null) {
      existing.buy(quantity, stock.getSalesPrice());
    }
    Purchase tx = (Purchase) TransactionFactory
        .createTransaction(TransactionType.PURCHASE, share, week.get());
    tx.commit(player, BigDecimal.ZERO, BigDecimal.ZERO);
  }

  /**
   * Executes a sale transaction transferring a held share back to the exchange.
   *
   * @param share the share to sell
   * @param amount the amount to sell
   * @param player the player that sells
   * @throws NullPointerException if any parameter is null
   */
  public void sell(Share share, BigDecimal amount, Player player) throws NullPointerException {
    Objects.requireNonNull(share, "Share cannot be null");
    Objects.requireNonNull(player, "Player cannot be null");
    Objects.requireNonNull(amount, "Amount cannot be null");

    if (share.quantity().compareTo(amount) == 0) {
      Sale tx = (Sale) TransactionFactory
          .createTransaction(TransactionType.SALE, share, week.get());
      tx.commit(player, commission, tax);
    } else {
      Share sellLot = new Share(share.stock(), amount, share.pricePerShare());
      share.sell(amount);
      Sale tx = (Sale) TransactionFactory
          .createTransaction(TransactionType.SALE, sellLot, week.get());
      tx.commit(player, commission, tax);
    }
  }

  /** Commission rate charged on each transaction. */
  public BigDecimal getCommission() {
    return commission;
  }

  /** Tax rate applied to net proceeds on sales. */
  public BigDecimal getTax() {
    return tax;
  }

  /**
   * Advances the calendar week and ticks every listed stock through the simulator
   * update per advance.
   */
  public void advance() {
    week.set(week.get() + 1);

    for (Stock stock : stockMap.values()) {
      double stackedVolatility = 1.0;
      double stackedImpactJump = 0.0;
      
      Random rnd = new java.util.Random();

      if (news != null) {
        for (NewsItem item : news) {
          if (item.sector() != null && !item.isExpired()
              && stock.getCategories().contains(item.sector())) {
            
            if (item.volatility() != null) {
              stackedVolatility *= item.volatility().doubleValue();
            }

            if (!item.appliedChange() && item.changePercent() != null) {
              double baseChange = item.changePercent().doubleValue();
              double finalIndividualChange = rnd.nextGaussian(baseChange, 0.05);
              
              stackedImpactJump += finalIndividualChange;
            }
          }
        }
      }

      double finalVolatilityModifier = this.volatilityMultiplier * stackedVolatility;
      BigDecimal baseNextPrice = simulator.nextPrice(stock, finalVolatilityModifier);

      BigDecimal impactMultiplier = BigDecimal.ONE.add(BigDecimal.valueOf(stackedImpactJump));
      BigDecimal finalNextPrice = baseNextPrice.multiply(impactMultiplier);

      if (finalNextPrice.signum() > 0) {
        stock.addNewSalesPrice(finalNextPrice);
      }
    }

    if (news != null) {
      for (NewsItem item : news) {
        if (!item.appliedChange()) {
          item.setAppliedChange(true);
        }
      }
    }
  }

  /**
   * Resets the exchange. Used when exiting a game.
   * */
  public void reset() {
    week.set(1);
    volatilityMultiplier = 1.0;
    for (Stock stock : stockMap.values()) {
      stock.reset();
    }
  }
}
