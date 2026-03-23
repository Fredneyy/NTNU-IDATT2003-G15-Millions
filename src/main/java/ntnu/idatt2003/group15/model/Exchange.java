package ntnu.idatt2003.group15.model;

import ntnu.idatt2003.group15.model.exceptions.BlankArgumentException;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

public class Exchange {
    private String name;
    private int week = 1;
    private final Map<String, Stock> stockMap;
    private Random random;

    public Exchange(String name, List<Stock> stocks) throws BlankArgumentException, NullPointerException {
        Objects.requireNonNull(name, "name cannot be null");
        if (name.isBlank()) {
            throw new BlankArgumentException("Name cannot be blank");
        }
        Objects.requireNonNull(stocks, "stocks cannot be null");
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
                    entry.getKey().contains(searchTerm) ||
                                entry.getValue().getCompany().contains(searchTerm))
                .map(Map.Entry::getValue)
                .toList();
    }

    public Purchase buy(String symbol, BigDecimal quantity, Player player) throws NullPointerException {
        Objects.requireNonNull(symbol, "Symbol cannot be null");
        Objects.requireNonNull(quantity, "Quantity cannot be null");
        Objects.requireNonNull(player, "Player cannot be null");
        Stock stock = getStock(symbol);
        Share share = new Share(stock, quantity, stock.getSalesPrice());
        Purchase tx = new Purchase(share, getWeek());
        tx.commit(player, BigDecimal.ZERO, BigDecimal.ZERO);
        return tx;
    }

    public Sale sell(Share share, Player player) throws NullPointerException {
        Objects.requireNonNull(share, "Share cannot be null");
        Objects.requireNonNull(player, "Player cannot be null");
        Stock stock = share.getStock();
        Sale tx = new Sale(share, getWeek(), stock.getSalesPrice());
        tx.commit(player , BigDecimal.ZERO, BigDecimal.ZERO);
        return tx;
    }

    public void advance() {
        this.week = getWeek() + 1;
    }

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

    public List<Stock> getLosers(int limit) {
        List<Stock> list = new ArrayList<>();
        stockMap.forEach((s, stock) -> {
            list.add(stock);
        });

        return list.stream()
                .sorted(Comparator.comparing(Stock::getLatestPriceChangeRelative))
                .limit(limit)
                .collect(Collectors.toList());
    }
}
