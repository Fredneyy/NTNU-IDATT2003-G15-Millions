package ntnu.idatt2003.group15.model;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import ntnu.idatt2003.group15.model.exceptions.BlankArgumentException;

/**
 * Represents a stock exchange managing a collection of active stocks
 * and their weekly trading values.
 */
public class Exchange {
  private final String name;
  private int week = 1;
  private final Map<String, Stock> stockMap;

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
    Random random = new Random();
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
  public int getWeek() {
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
    Purchase tx = new Purchase(share, getWeek());
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
    Sale tx = new Sale(share, getWeek());
    tx.commit(player, BigDecimal.ZERO, BigDecimal.ZERO);
    return tx;
  }

  /**
   * Advances the calendar week, triggering price reevaluations across listed stocks.
   */
  public void advance() {
    this.week = getWeek() + 1;
  }

  /**
   * Retrieves the top gaining stocks bounded by the given limit.
   *
   * @param limit the amount of stocks to retrieve
   * @return a {@code List} containing the best to worst stocks
   */
  public List<Stock> getGainers(int limit) {
    List<Stock> list = new ArrayList<>();
    stockMap.forEach((s, stock) -> {
      list.add(stock);
    });

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
    stockMap.forEach((s, stock) -> list.add(stock));

    return list.stream()
        .sorted(Comparator.comparing(Stock::getLatestPriceChangeRelative))
        .limit(limit)
        .collect(Collectors.toList());
  }
}
