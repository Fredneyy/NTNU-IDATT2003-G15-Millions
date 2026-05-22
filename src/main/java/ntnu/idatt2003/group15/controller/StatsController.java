package ntnu.idatt2003.group15.controller;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import ntnu.idatt2003.group15.model.transactions.Purchase;
import ntnu.idatt2003.group15.model.transactions.Sale;
import ntnu.idatt2003.group15.model.stocks.Share;
import ntnu.idatt2003.group15.model.transactions.Transaction;
import ntnu.idatt2003.group15.view.StatsView;
import ntnu.idatt2003.group15.view.StatsView.Tone;

/**
 * Reactive glue between the player's transaction archive / portfolio and the
 * {@link StatsView}. Recomputes KPI cards, the performance summary, and the
 * holdings table whenever transactions are committed or share lots change.
 *
 * <p>Win/loss is judged per sale: profitable sale → win, otherwise → loss.
 * Realized P/L is the sum of per-sale {@link Sale#getRealizedPnl()} (net of fees).
 * Total return is computed against the player's starting balance so cash that
 * went out and came back lossy is reflected even after holdings drop to zero.
 */
public class StatsController {

  private final StatsView view;
  private final PlayerController player;
  private final ExchangeController exchange;

  public StatsController(StatsView view, PlayerController player, ExchangeController exchange) {
    this.view = Objects.requireNonNull(view);
    this.player = Objects.requireNonNull(player);
    this.exchange = Objects.requireNonNull(exchange);
    bind();
    refresh();
  }

  private void bind() {
    player.getTransactionArchive().getTransactionsProperty()
        .addListener((ListChangeListener<Transaction>) _ -> refresh());

    player.getPortfolio().getListProperty()
        .addListener((ListChangeListener<Share>) _ -> refresh());

    ChangeListener<Object> any = (_, _, _) -> refresh();
    player.getNetWorthProperty().addListener(any);
    player.getPlayer().getPortfolio().getUnrealizedPnlProperty().addListener(any);
  }

  /** Recompute every visible figure from scratch. Cheap given typical trade counts. */
  public void refresh() {
    var txs = player.getTransactionArchive().getTransactionsProperty();
    int buys = 0;
    int sells = 0;
    int wins = 0;
    int losses = 0;
    BigDecimal realized = BigDecimal.ZERO;
    BigDecimal volume = BigDecimal.ZERO;
    Map<String, BigDecimal> volumeBySymbol = new HashMap<>();

    for (Transaction tx : txs) {
      BigDecimal qty = tx.getShare().quantity();
      String symbol = tx.getShare().stock().getSymbol();

      if (tx instanceof Sale s) {
        sells++;
        BigDecimal salePrice = s.getSalePricePerShare();
        BigDecimal tradeValue = (salePrice == null ? tx.getShare().pricePerShare() : salePrice)
            .multiply(qty);
        volume = volume.add(tradeValue);
        volumeBySymbol.merge(symbol, tradeValue, BigDecimal::add);

        BigDecimal pnl = s.getRealizedPnl();
        realized = realized.add(pnl);
        if (pnl.signum() > 0) wins++;
        else if (pnl.signum() < 0) losses++;
      } else if (tx instanceof Purchase) {
        buys++;
        BigDecimal tradeValue = tx.getShare().pricePerShare().multiply(qty);
        volume = volume.add(tradeValue);
        volumeBySymbol.merge(symbol, tradeValue, BigDecimal::add);
      }
    }

    int total = buys + sells;
    view.setTotalTrades(total, buys, sells);
    view.setRealizedPL(formatSigned(realized), tone(realized.signum()));

    BigDecimal unrealized = nz(player.getPlayer().getPortfolio().getUnrealizedPnlProperty().getValue());
    view.setUnrealizedPL(formatSigned(unrealized), tone(unrealized.signum()));

    int rated = wins + losses;
    BigDecimal winRate = rated == 0
        ? BigDecimal.ZERO
        : BigDecimal.valueOf(wins).movePointRight(2)
              .divide(BigDecimal.valueOf(rated), 1, RoundingMode.HALF_UP);
    view.setWinRate(winRate.toPlainString() + "%", wins, losses);

    BigDecimal starting = nz(player.getStartingMoney());
    BigDecimal netWorth = nz(player.getNetWorthProperty().getValue());
    BigDecimal totalReturn = netWorth.subtract(starting);
    BigDecimal pct = starting.signum() == 0
        ? BigDecimal.ZERO
        : totalReturn.divide(starting, 4, RoundingMode.HALF_UP).movePointRight(2);
    view.setTotalReturn(
        formatSigned(totalReturn),
        "(" + (pct.signum() >= 0 ? "+" : "")
            + pct.setScale(2, RoundingMode.HALF_UP).toPlainString() + "%)",
        tone(totalReturn.signum()));

    BigDecimal avg = total == 0
        ? BigDecimal.ZERO
        : volume.divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);
    view.setAvgTradeSize("$" + avg.setScale(2, RoundingMode.HALF_UP).toPlainString());

    view.setMostTraded(pickMostTraded(volumeBySymbol));
    view.setHoldings(player.getPortfolio().getShares());
  }

  private static String pickMostTraded(Map<String, BigDecimal> volumeBySymbol) {
    String best = null;
    BigDecimal max = BigDecimal.ZERO;
    for (var e : volumeBySymbol.entrySet()) {
      if (e.getValue().compareTo(max) > 0) {
        max = e.getValue();
        best = e.getKey();
      }
    }
    return best == null ? "—" : best;
  }

  private static Tone tone(int signum) {
    if (signum > 0) return Tone.POSITIVE;
    if (signum < 0) return Tone.NEGATIVE;
    return Tone.POSITIVE; // zero shows as neutral-positive "+$0.00"
  }

  private static String formatSigned(BigDecimal v) {
    BigDecimal n = nz(v);
    String sign = n.signum() >= 0 ? "+" : "-";
    return sign + "$" + n.abs().setScale(2, RoundingMode.HALF_UP).toPlainString();
  }

  private static BigDecimal nz(BigDecimal v) {
    return v == null ? BigDecimal.ZERO : v;
  }
}
