package ntnu.idatt2003.group15.model;

import com.sun.javafx.iio.ImageLoadListener;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;

public class Exchange {
  private String name;
  private int week = 1;
  private Map<String, Stock> stockMap;
  private Random random;

  public void Exchange(String name, List<Stock> stocks) throws IllegalArgumentException {
      if (name == null || name.isBlank()) {
          throw new IllegalArgumentException("Name cannot be blank or null");
      }

    this.name = name;
    this.stockMap = stocks.stream().collect(Collectors.toMap(Stock::getSymbol, stock -> stock));
    this.random = new Random();
  }

  public String getName() {
    return name;
  }

  public int getWeek() {
    return week;
  }

  public boolean hasStock(String symbol) throws NullPointerException{
    return this.stockMap.containsKey(symbol);
  }

  public Stock getStock(String symbol) throws NullPointerException{
    return this.stockMap.get(symbol);
  }

  public List<Stock> findStocks(String searchTerm) throws NullPointerException{
    Objects.requireNonNull(searchTerm, "Search term cannot be null");
    return stockMap.entrySet().stream()
        .filter(entry ->
                entry.getKey().equals(searchTerm) ||
                entry.getValue().getCompany().equalsIgnoreCase(searchTerm))
        .map(Map.Entry::getValue)
        .toList();
  }

  // Kjøpspris er hardkodet og må oppdateres
  public Purchase buy(String symbol, BigDecimal quantity, Player player) throws NullPointerException {
    Objects.requireNonNull(symbol, "Symbol cannot be null");
    Objects.requireNonNull(quantity, "Quantity cannot be null");
    Objects.requireNonNull(player, "Player cannot be null");
    Stock stock = getStock(symbol);
    Share share = new Share(stock, quantity, BigDecimal.valueOf(100));
    Purchase tx = new Purchase(share, getWeek());
    tx.commit(player);
    return tx;
  }

  // Salgsprislisten oppdateres ikke
  public Sale sell(Share share, Player player) throws NullPointerException {
    Objects.requireNonNull(share, "Share cannot be null");
    Objects.requireNonNull(player, "Player cannot be null");
    Stock stock = share.getStock();
    Sale tx = new Sale(share, getWeek(), stock.getSalesPrice());
    tx.commit(player);
    return tx;
  }

  public void advance() {
    this.week = getWeek() + 1;
  }
}
