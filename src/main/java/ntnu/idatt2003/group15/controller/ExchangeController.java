package ntnu.idatt2003.group15.controller;

import javafx.beans.value.ObservableIntegerValue;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import ntnu.idatt2003.group15.model.*;
import ntnu.idatt2003.group15.model.news.NewsItem;
import ntnu.idatt2003.group15.model.player.Player;
import ntnu.idatt2003.group15.model.stocks.Share;
import ntnu.idatt2003.group15.model.stocks.Stock;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * Controller mediating interactions between the UI and the {@link Exchange}.
 * Delegates all business logic to the model layer on behalf of the active {@link Player}.
 */
public class ExchangeController {
    private final Exchange exchange;

    public ExchangeController(Exchange exchange) {
        this.exchange = Objects.requireNonNull(exchange, "Exchange cannot be null");
    }

    public String getExchangeName() {
        return exchange.getName();
    }

    public ObservableIntegerValue getWeek() {
        return exchange.getWeekProperty();
    }

    public List<Stock> getAllStocks() {
        return exchange.getAllStocks();
    }

    public List<Stock> getGainers(int limit) {
        return exchange.getGainers(limit);
    }

    public List<Stock> getLosers(int limit) {
        return exchange.getLosers(limit);
    }

    public void buy(Stock stock, BigDecimal quantity, Player player) {
        exchange.buy(Objects.requireNonNull(stock.getSymbol()), Objects.requireNonNull(quantity), Objects.requireNonNull(player));
    }

    public void sell(Share share, BigDecimal amount, Player player) {
        Objects.requireNonNull(share, "Share cannot be null");
        Objects.requireNonNull(amount, "Amount cannot be null");
        if (!player.getPortfolio().contains(share)) {
            throw new IllegalArgumentException("Player does not own this share");
        }
        exchange.sell(share, amount, player);
    }

    public BigDecimal getCommission() {
        return exchange.getCommission();
    }

    public BigDecimal getTax() {
        return exchange.getTax();
    }

    public void advanceWeek() {
        exchange.advance();
    }

    public void setVolatilityMultiplier(double volatilityMultiplier) {
        exchange.setVolatilityMultiplier(volatilityMultiplier);
    }

    public void setNewsObserver(ObservableList<NewsItem> newsObserver) {
        exchange.setNewsObservableList(newsObserver);
    }
}
