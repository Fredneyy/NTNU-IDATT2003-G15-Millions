package ntnu.idatt2003.group15.model;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import com.sun.jdi.IntegerValue;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableIntegerValue;
import ntnu.idatt2003.group15.model.exceptions.BlankArgumentException;
import ntnu.idatt2003.group15.model.factories.TransactionFactory;
import ntnu.idatt2003.group15.model.factories.TransactionType;

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
      throw new BlankArgumentException("Name cannot be blank");
    }
    Objects.requireNonNull(stocks, "stocks cannot be null");
    this.name = name;
    this.stockMap = stocks.stream().collect(Collectors.toMap(Stock::getSymbol, stock -> stock));
  }

  /**
   * Retrieves the name of the exchange.
   *
   * @return the name of the exchange
   */
  public String getName() {
    return name;
  }

  /**
   * Retrieves the current simulation week of the exchange.
   *
   * @return the current week
   */
  public ObservableIntegerValue getWeekProperty() {
    return week;
  }

  /**
   * Checks if a stock is listed on this exchange based on its symbol.
   *
   * @param symbol the symbol to search for
   * @return if the stock exists in the map
   */
  public boolean hasStock(String symbol) throws NullPointerException {
    return this.stockMap.containsKey(symbol);
  }

  /**
   * Retrieves a listed stock using its unique ticker symbol.
   *
   * @param symbol the symbol to search for
   * @return the {@code Stock} in the map matching the symbol
   */
  public Stock getStock(String symbol) throws NullPointerException {
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
   * @return the {@code Purchase}
   */
  public Purchase buy(String symbol, BigDecimal quantity, Player player)
      throws NullPointerException {
    Objects.requireNonNull(symbol, "Symbol cannot be null");
    Objects.requireNonNull(quantity, "Quantity cannot be null");
    Objects.requireNonNull(player, "Player cannot be null");
    Stock stock = getStock(symbol);
    Share share = new Share(stock, quantity, stock.getSalesPrice());
    Purchase tx = (Purchase) TransactionFactory.createTransaction(TransactionType.PURCHASE, share, week.get());
    tx.commit(player, BigDecimal.ZERO, BigDecimal.ZERO);
    return tx;
  }

  /**
   * Executes a sale transaction transferring a held share back to the exchange.
   *
   * @param share the share to sell
   * @param player the player that sells
   * @return the {@code Sale}
   */
  public Sale sell(Share share, Player player) throws NullPointerException {
    Objects.requireNonNull(share, "Share cannot be null");
    Objects.requireNonNull(player, "Player cannot be null");
    Sale tx = (Sale) TransactionFactory.createTransaction(TransactionType.SALE, share, week.get());
    tx.commit(player, BigDecimal.ZERO, BigDecimal.ZERO);
    return tx;
  }

  /**
   * Advances the calendar week and ticks every listed stock through the simulator,
   * pushing the new price onto the stock when positive. Any active news-driven
   * volatility windows registered via {@link #applyNews(NewsItem)} decay one
   * update per advance.
   */
  public void advance() {
    week.set(week.get() + 1);
    for (Stock stock : stockMap.values()) {
      BigDecimal next = simulator.nextPrice(stock);
      if (next.signum() > 0) {
        stock.addNewSalesPrice(next);
      }
    }
  }

  /**
   * Apply a news headline to every stock whose categories include
   * {@code item.sector()}: each affected stock takes an immediate
   * {@code changePercent} price shock and enters an elevated-volatility window.
   */
  public void applyNews(NewsItem item) {
    Objects.requireNonNull(item, "news item cannot be null");
    simulator.applyNews(item, List.copyOf(stockMap.values()));
  }

  /**
   * Retrieves the top gaining stocks bounded by the given limit.
   *
   * @param limit the amount of stocks to retrieve
   * @return a {@code List} containing the best to worst stocks
   */
  public List<Stock> getGainers(int limit) {
    List<Stock> list = new ArrayList<>();
    stockMap.forEach((_, stock) -> list.add(stock));

    return list.stream()
        .sorted(Comparator.comparing(Stock::getLatestPriceChangeRelative).reversed())
        .limit(limit)
        .collect(Collectors.toList());
  }

  /**
   * Retrieves the top losing stocks bounded by the given limit.
   *
   * @param limit the amount of stocks to retrieve
   * @return a {@code List} containing the worst to best stocks
   */
  public List<Stock> getLosers(int limit) {
    List<Stock> list = new ArrayList<>();
    stockMap.forEach((_, stock) -> list.add(stock));

    return list.stream()
        .sorted(Comparator.comparing(Stock::getLatestPriceChangeRelative))
        .limit(limit)
        .collect(Collectors.toList());
  }
}
