package ntnu.idatt2003.group15.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;

import ntnu.idatt2003.group15.model.Exchange;
import ntnu.idatt2003.group15.model.GameSettings;
import ntnu.idatt2003.group15.model.Player;
import ntnu.idatt2003.group15.model.Portfolio;
import ntnu.idatt2003.group15.model.Purchase;
import ntnu.idatt2003.group15.model.Sale;
import ntnu.idatt2003.group15.model.SaveData;
import ntnu.idatt2003.group15.model.Share;
import ntnu.idatt2003.group15.model.Stock;
import ntnu.idatt2003.group15.model.exceptions.BlankArgumentException;

/**
 * Controller mediating the main menu, where a new {@link Player} is created before the game starts.
 * Validates the player name and produces the controllers needed to run the game.
 */
public class MainMenuController {

  private final BigDecimal DEFAULT_STARTING_MONEY = new BigDecimal("10000");
  private final BiConsumer<ExchangeController, PlayerController> onGameStartConsumer;
  private BiConsumer<ExchangeController, PlayerController> onGameLoadConsumer;

  private final Exchange exchange;
  private GameSettings gameSettings;

  /**
   * Constructs a main-menu controller bound to the given exchange.
   *
   * @param exchange the exchange that will be used for the game session
   * @throws NullPointerException if exchange is null
   */
  public MainMenuController(Exchange exchange, BiConsumer<ExchangeController, PlayerController> onGameStartConsumer) {
    this.exchange = Objects.requireNonNull(exchange, "Exchange cannot be null");
    this.onGameStartConsumer = onGameStartConsumer;
  }

  /** Optional settings hook; if set, {@link #loadGame(SaveData)} will restore difficulty into it. */
  public void setGameSettings(GameSettings settings) {
    this.gameSettings = settings;
  }

  /**
   * Register a separate hand-off used by {@link #loadGame(SaveData)}. If unset,
   * loaded games fall back to the new-game consumer.
   */
  public void setOnGameLoadConsumer(BiConsumer<ExchangeController, PlayerController> consumer) {
    this.onGameLoadConsumer = consumer;
  }

  /**
   * Creates a new {@link Player} with the given name and starting balance, then notifies
   * the registered consumer with an {@link ExchangeController} and {@link PlayerController}
   * scoped to that player. If {@code customStocks} is non-null and non-empty, a fresh
   * {@link Exchange} is built from them for this session; otherwise the default exchange
   * is used.
   *
   * @param name the name entered by the user
   * @param startingMoney the starting balance, or {@code null} to use the default
   * @param customStocks user-supplied stocks for this session, or {@code null} for default
   * @throws NullPointerException   if name is null
   * @throws BlankArgumentException if name is blank
   */
  public void startGame(String name, BigDecimal startingMoney, List<Stock> customStocks)
      throws BlankArgumentException, NullPointerException {
    Objects.requireNonNull(name, "Name cannot be null");
    if (startingMoney == null) {
      startingMoney = DEFAULT_STARTING_MONEY;
    }
    Player player = new Player(name, startingMoney);
    PlayerController playerController = createPlayerController(player);
    Exchange exchangeForGame = (customStocks == null || customStocks.isEmpty())
        ? this.exchange
        : new Exchange(this.exchange.getName(), customStocks);
    onGameStartConsumer.accept(new ExchangeController(exchangeForGame, player), playerController);
  }

  /**
   * Returns a {@link PlayerController} for the given name without starting a full game session.
   * Useful when the UI needs to preview player state before entering the exchange view.
   *
   * @param player the player object
   * @return a {@link PlayerController} wrapping the newly created player
   * @throws NullPointerException   if name is null
   */
  public PlayerController createPlayerController(Player player) throws NullPointerException {
    Objects.requireNonNull(player, "Player cannot be null");
    return new PlayerController(player);
  }

  /**
   * Validates whether the given name is acceptable for a new player.
   *
   * @param name the name to validate
   * @return {@code true} if the name is non-null and non-blank, {@code false} otherwise
   */
  public boolean isValidName(String name) {
    return name != null && !name.isBlank();
  }

  /**
   * Returns the default starting money awarded to every new player.
   *
   * @return the default starting balance
   */
  public BigDecimal getDefaultStartingMoney() {
    return DEFAULT_STARTING_MONEY;
  }

  /**
   * Restore a previously saved game: rebuild the player, override stock prices,
   * restore the simulation week, then hand off to the start-game consumer.
   *
   * <p>Caller is responsible for applying difficulty/other settings.
   */
  public void loadGame(SaveData save) {
    Objects.requireNonNull(save, "save");
    String name = (save.playerName() == null || save.playerName().isBlank())
        ? "Trader" : save.playerName();
    BigDecimal cash = save.cash() == null ? DEFAULT_STARTING_MONEY : save.cash();
    // Preserve the original starting balance so Total Return / % stay anchored
    // to the same number after reload. Older saves without this field fall back
    // to the current cash (i.e. baseline = current — % shows as 0 until trades happen).
    BigDecimal startingMoney = save.startingMoney() != null
        ? save.startingMoney() : cash;
    Player player = new Player(name, startingMoney);
    // Player constructor seeds money = startingMoney; reconcile to saved cash.
    BigDecimal diff = cash.subtract(startingMoney);
    if (diff.signum() > 0) player.addMoney(diff);
    else if (diff.signum() < 0) player.withdrawMoney(diff.negate());

    // Overlay saved stock prices onto the live exchange's stocks.
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

    // Reattach owned share lots.
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

    // Replay the trade ledger directly into the archive — bypassing commit() so
    // we don't double-move cash or duplicate portfolio lots (both already restored above).
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
    handoff.accept(new ExchangeController(exchange, player), playerController);
  }
}
