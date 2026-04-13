package ntnu.idatt2003.group15.controller;

import javafx.collections.ObservableList;
import ntnu.idatt2003.group15.model.Exchange;
import ntnu.idatt2003.group15.model.Player;
import ntnu.idatt2003.group15.model.Share;
import ntnu.idatt2003.group15.model.Stock;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * Controller mediating interactions between the UI and the {@link Exchange}.
 * Delegates all business logic to the model layer on behalf of the active {@link Player}.
 */
public class ExchangeController {
    private final Exchange exchange;
    private final Player player;

    /**
     * Constructs a controller bound to the given exchange and player.
     *
     * @param exchange the exchange to operate on
     * @param player   the player performing actions on the exchange
     */
    public ExchangeController(Exchange exchange, Player player) {
        this.exchange = exchange;
        this.player = player;
    }

    /**
     * Returns the name of the exchange.
     *
     * @return the exchange name
     */
    public String getExchangeName() {
        return exchange.getName();
    }

    /**
     * Returns the current simulation week.
     *
     * @return the current week number
     */
    public int getWeek() {
        return exchange.getWeek();
    }

    /**
     * Returns all stocks currently listed on the exchange.
     *
     * @return a list of all listed stocks
     */
    public List<Stock> getAllStocks() {
        return exchange.getAllStocks();
    }

    /**
     * Searches the exchange for stocks whose symbol or company name contains the given query.
     *
     * @param searchQuery the search term to match against
     * @return a list of matching stocks
     * @throws NullPointerException if searchQuery is null
     */
    public List<Stock> findStocks(String searchQuery) {
        Objects.requireNonNull(searchQuery, "Search query cannot be null");
        return exchange.findStocks(searchQuery);
    }

    /**
     * Returns the top gaining stocks for the current week, bounded by the given limit.
     *
     * @param limit the maximum number of stocks to return
     * @return a list of stocks sorted from highest to lowest relative price change
     */
    public List<Stock> getGainers(int limit) {
        return exchange.getGainers(limit);
    }

    /**
     * Returns the top losing stocks for the current week, bounded by the given limit.
     *
     * @param limit the maximum number of stocks to return
     * @return a list of stocks sorted from lowest to highest relative price change
     */
    public List<Stock> getLosers(int limit) {
        return exchange.getLosers(limit);
    }

    /**
     * Purchases the given quantity of a stock at its current market price on behalf of the player.
     *
     * @param stock    the stock to purchase
     * @param quantity the number of shares to buy
     * @throws NullPointerException if stock or quantity is null
     */
    public void buy(Stock stock, BigDecimal quantity) {
        Objects.requireNonNull(stock, "Stock cannot be null");
        Objects.requireNonNull(quantity, "Quantity cannot be null");
        exchange.buy(stock.getSymbol(), quantity, player);
    }

    /**
     * Sells the given share on behalf of the player.
     *
     * @param share the share to sell
     * @throws NullPointerException     if share is null
     * @throws IllegalArgumentException if the player does not own the share
     */
    public void sell(Share share) {
        Objects.requireNonNull(share, "Share cannot be null");
        if (!player.getPortfolio().contains(share)) {
            throw new IllegalArgumentException("Player does not own this share");
        }
        exchange.sell(share, player);
    }

    /**
     * Returns the observable list of shares held in the player's portfolio, suitable for UI binding.
     *
     * @return the player's portfolio shares as an observable list
     */
    public ObservableList<Share> getPortfolioShares() {
        return player.getPortfolio().getListProperty();
    }

    /**
     * Advances the simulation by one week, triggering price updates across all listed stocks.
     */
    public void advanceWeek() {
        exchange.advance();
    }
}
