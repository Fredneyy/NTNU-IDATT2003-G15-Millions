package ntnu.idatt2003.group15.controller;

import javafx.beans.value.ObservableIntegerValue;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import ntnu.idatt2003.group15.model.*;

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
     * Constructs a controller bound to the given exchange and player. The player is needed
     * to expose session-level observables (net worth, cash, P/L, etc.) that the view binds to.
     *
     * @param exchange the exchange to operate on
     * @param player the player whose session-level stats this controller exposes
     */
    public ExchangeController(Exchange exchange, Player player) {
        this.exchange = Objects.requireNonNull(exchange, "Exchange cannot be null");
        this.player = Objects.requireNonNull(player, "Player cannot be null");
    }

    public ObservableValue<BigDecimal> netWorthProperty() {
        return player.getNetWorthProperty();
    }

    public ObservableValue<BigDecimal> cashProperty() {
        return player.getCashProperty();
    }

    public ObservableValue<BigDecimal> portfolioValueProperty() {
        return player.getPortfolio().getTotalMarketValueProperty();
    }

    public ObservableValue<BigDecimal> investedProperty() {
        return player.getPortfolio().getInvestedProperty();
    }

    public ObservableValue<BigDecimal> unrealizedPnlProperty() {
        return player.getPortfolio().getUnrealizedPnlProperty();
    }

    public ObservableValue<BigDecimal> unrealizedPnlPercentProperty() {
        return player.getPortfolio().getUnrealizedPnlPercentProperty();
    }

    public ObservableValue<BigDecimal> netWorthChangeProperty() {
        return player.getNetWorthChangeProperty();
    }

    public ObservableValue<BigDecimal> netWorthChangePercentProperty() {
        return player.getNetWorthChangePercentProperty();
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
    public ObservableIntegerValue getWeek() {
        return exchange.getWeekProperty();
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
    public void buy(Stock stock, BigDecimal quantity, Player player) {
        Objects.requireNonNull(stock, "Stock cannot be null");
        Objects.requireNonNull(quantity, "Quantity cannot be null");
        exchange.buy(stock.getSymbol(), quantity, player);
    }

    /**
     * Purchases the given quantity of a stock at its current market price for the
     * player bound to this controller.
     */
    public void buy(Stock stock, BigDecimal quantity) {
        buy(stock, quantity, player);
    }

    /**
     * Sells the given share on behalf of the player.
     *
     * @param share the share to sell
     * @throws NullPointerException     if share is null
     * @throws IllegalArgumentException if the player does not own the share
     */
    public void sell(Share share, BigDecimal amount) {
        Objects.requireNonNull(share, "Share cannot be null");
        Objects.requireNonNull(amount, "Amount cannot be null");
        if (!player.getPortfolio().contains(share)) {
            throw new IllegalArgumentException("Player does not own this share");
        }
        exchange.sell(share, amount, player);
    }

    /**
     * Returns the observable list of shares held in the player's portfolio, suitable for UI binding.
     *
     * @return the player's portfolio shares as an observable list
     */
    public ObservableList<Share> getPortfolioShares(Player player) {
        return player.getPortfolio().getListProperty();
    }

    /** Commission rate the exchange charges on each transaction. */
    public BigDecimal getCommission() {
        return exchange.getCommission();
    }

    /** Tax rate the exchange applies to sale proceeds. */
    public BigDecimal getTax() {
        return exchange.getTax();
    }

    /**
     * Advances the simulation by one week, triggering price updates across all listed stocks.
     */
    public void advanceWeek() {
        exchange.advance();
    }

    public void setVolatilityMultiplier(double volatilityMultiplier) {
        exchange.setVolatilityMultiplier(volatilityMultiplier);
    }
}
