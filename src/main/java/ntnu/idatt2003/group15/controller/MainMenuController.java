package ntnu.idatt2003.group15.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;

import ntnu.idatt2003.group15.model.Exchange;
import ntnu.idatt2003.group15.model.GameSettings;
import ntnu.idatt2003.group15.model.player.Player;
import ntnu.idatt2003.group15.model.player.Portfolio;
import ntnu.idatt2003.group15.model.transactions.Purchase;
import ntnu.idatt2003.group15.model.transactions.Sale;
import ntnu.idatt2003.group15.model.SaveData;
import ntnu.idatt2003.group15.model.stocks.Share;
import ntnu.idatt2003.group15.model.stocks.Stock;
import ntnu.idatt2003.group15.model.exceptions.BlankArgumentException;

/**
 * Controller mediating the main menu, where a new {@link Player} is created before the game starts.
 * Validates the player name and produces the controllers needed to run the game.
 */
public class MainMenuController {

  private final BiConsumer<ExchangeController, PlayerController> onGameStartConsumer;
  private BiConsumer<ExchangeController, PlayerController> onGameLoadConsumer;

  private final Exchange exchange;
  private GameSettings gameSettings;

  public MainMenuController(Exchange exchange, BiConsumer<ExchangeController, PlayerController> onGameStartConsumer) {
    this.exchange = Objects.requireNonNull(exchange, "Exchange cannot be null");
    this.onGameStartConsumer = onGameStartConsumer;
  }

  public void setGameSettings(GameSettings settings) {
    this.gameSettings = settings;
  }

  public void setOnGameLoadConsumer(BiConsumer<ExchangeController, PlayerController> consumer) {
    this.onGameLoadConsumer = consumer;
  }

  public void startGame(String name, BigDecimal startingMoney, List<Stock> customStocks)
      throws BlankArgumentException, NullPointerException {
    Player player = new Player(name, startingMoney);
    PlayerController playerController = createPlayerController(player);
    Exchange exchangeForGame = (customStocks == null || customStocks.isEmpty())
        ? this.exchange
        : new Exchange(this.exchange.getName(), customStocks);
    onGameStartConsumer.accept(new ExchangeController(exchangeForGame), playerController);
  }

  public PlayerController createPlayerController(Player player) throws NullPointerException {
    Objects.requireNonNull(player, "Player cannot be null");
    return new PlayerController(player);
  }

  public void loadGame(SaveData save) {
    Objects.requireNonNull(save, "save");
    String name = (save.playerName() == null || save.playerName().isBlank())
        ? "Trader" : save.playerName();
    BigDecimal cash = save.cash();
    BigDecimal startingMoney = save.startingMoney() != null
        ? save.startingMoney() : cash;
    Player player = new Player(name, startingMoney);

    BigDecimal diff = cash.subtract(startingMoney);
    if (diff.signum() > 0) player.addMoney(diff);
    else if (diff.signum() < 0) player.withdrawMoney(diff.negate());

    if (save.stockPrices() != null) {
      for (var entry : save.stockPrices().entrySet()) {
        if (!exchange.hasStock(entry.getKey())) continue;
        Stock stock = exchange.getStock(entry.getKey());
        BigDecimal price = entry.getValue();
        if (price != null && price.signum() > 0) {
          stock.addNewSalesPrice(price);
        }
      }
    }

    Portfolio portfolio = player.getPortfolio();
    if (save.shares() != null) {
      for (SaveData.ShareEntry s : save.shares()) {
        if (!exchange.hasStock(s.symbol())) continue;
        portfolio.addShare(new Share(exchange.getStock(s.symbol()), s.quantity(), s.pricePerShare()));
      }
    }

    if (save.week() != null && save.week() > 0) {
      exchange.setWeek(save.week());
    }

    if (save.transactions() != null) {
      for (SaveData.TxEntry t : save.transactions()) {
        if (!exchange.hasStock(t.symbol())) continue;
        if (t.quantity() == null || t.quantity().signum() <= 0) continue;
        if (t.pricePerShare() == null || t.pricePerShare().signum() <= 0) continue;
        Share lot = new Share(exchange.getStock(t.symbol()), t.quantity(), t.pricePerShare());
        int txWeek = t.week() == null ? 1 : t.week();
        if ("SELL".equalsIgnoreCase(t.type())) {
          player.getTransactionArchive().add(
              Sale.restored(lot, txWeek, t.committedAt(),
                  t.salePricePerShare(), t.proceeds()));
        } else {
          player.getTransactionArchive().add(
              Purchase.restored(lot, txWeek, t.committedAt()));
        }
      }
    }

    if (gameSettings != null && save.difficulty() != null) {
      gameSettings.setDifficulty(save.difficulty());
    }

    PlayerController playerController = createPlayerController(player);
    BiConsumer<ExchangeController, PlayerController> handoff =
        onGameLoadConsumer != null ? onGameLoadConsumer : onGameStartConsumer;
    handoff.accept(new ExchangeController(exchange), playerController);
  }
}
