package ntnu.idatt2003.group15.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import ntnu.idatt2003.group15.model.Exchange;
import ntnu.idatt2003.group15.model.GameSettings;
import ntnu.idatt2003.group15.model.SaveData;
import ntnu.idatt2003.group15.model.exceptions.BlankArgumentException;
import ntnu.idatt2003.group15.model.player.Player;
import ntnu.idatt2003.group15.model.stocks.Stock;
import ntnu.idatt2003.group15.model.stocks.StockSectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MainMenuControllerTest {

  private Stock apple;
  private Stock equinor;
  private Exchange exchange;
  private AtomicReference<ExchangeController> capturedExchange;
  private AtomicReference<PlayerController> capturedPlayer;
  private MainMenuController controller;

  @BeforeEach
  void setUp() {
    apple = new Stock("AAPL", "Apple", BigDecimal.valueOf(100), 0.0, 0.0,
        List.of(StockSectors.TECHNOLOGY));
    equinor = new Stock("EQNR", "Equinor", BigDecimal.valueOf(300), 0.0, 0.0,
        List.of(StockSectors.ENERGY));
    exchange = new Exchange("FREX", List.of(apple, equinor));
    capturedExchange = new AtomicReference<>();
    capturedPlayer = new AtomicReference<>();
    controller = new MainMenuController(exchange, (ex, pl) -> {
      capturedExchange.set(ex);
      capturedPlayer.set(pl);
    });
  }

  @Test
  void startGameInvokesConsumerWithFreshControllers() {
    controller.startGame("Alice", BigDecimal.valueOf(1000), null);

    assertNotNull(capturedExchange.get());
    assertNotNull(capturedPlayer.get());
    assertEquals("Alice", capturedPlayer.get().getName());
    assertEquals(0,
        BigDecimal.valueOf(1000).compareTo(capturedPlayer.get().getMoney()));
  }

  @Test
  void startGameWithCustomStocksUsesNewExchange() {
    Stock custom = new Stock("CST", "Custom", BigDecimal.valueOf(50), 0.0, 0.0,
        List.of(StockSectors.MACRO));
    controller.startGame("Bob", BigDecimal.valueOf(500), List.of(custom));

    assertEquals(1, capturedExchange.get().getAllStocks().size());
  }

  @Test
  void startGameWithEmptyCustomStockListFallsBackToDefaultExchange() {
    controller.startGame("Bob", BigDecimal.valueOf(500), List.of());

    assertEquals(2, capturedExchange.get().getAllStocks().size());
  }

  @Test
  void startGameRejectsBlankName() {
    assertThrows(BlankArgumentException.class,
        () -> controller.startGame("  ", BigDecimal.valueOf(1000), null));
  }

  @Test
  void startGameRejectsNullName() {
    assertThrows(NullPointerException.class,
        () -> controller.startGame(null, BigDecimal.valueOf(1000), null));
  }

  @Test
  void createPlayerControllerRejectsNullPlayer() {
    assertThrows(NullPointerException.class, () -> controller.createPlayerController(null));
  }

  @Test
  void createPlayerControllerWrapsPlayer() {
    Player player = new Player("Wrapped", BigDecimal.valueOf(10));
    PlayerController pc = controller.createPlayerController(player);
    assertEquals("Wrapped", pc.getName());
  }

  @Test
  void loadGameRestoresCashAndPortfolio() {
    SaveData save = new SaveData(
        "Restored",
        BigDecimal.valueOf(800),
        BigDecimal.valueOf(1000),
        null,
        5,
        Instant.now(),
        List.of(new SaveData.ShareEntry("AAPL", BigDecimal.valueOf(2), BigDecimal.valueOf(120))),
        Map.of("AAPL", BigDecimal.valueOf(150)),
        List.of());

    controller.loadGame(save);

    PlayerController player = capturedPlayer.get();
    assertEquals("Restored", player.getName());
    assertEquals(0, BigDecimal.valueOf(800).compareTo(player.getMoney()));
    assertEquals(0, BigDecimal.valueOf(1000).compareTo(player.getStartingMoney()));
    assertEquals(1, player.getPortfolio().getShares().size());
    assertEquals(5, capturedExchange.get().getWeek().get());
    // The latest sales price from the save is appended to the live exchange.
    assertEquals(0, BigDecimal.valueOf(150).compareTo(apple.getSalesPrice()));
  }

  @Test
  void loadGameWithBlankNameFallsBackToDefaultTrader() {
    SaveData save = new SaveData(
        "  ", BigDecimal.valueOf(100), BigDecimal.valueOf(100),
        null, null, null, List.of(), Map.of(), List.of());

    controller.loadGame(save);

    assertEquals("Trader", capturedPlayer.get().getName());
  }

  @Test
  void loadGameAppliesDifficultyWhenSettingsBound() {
    GameSettings settings = new GameSettings();
    controller.setGameSettings(settings);

    SaveData save = new SaveData(
        "Diff", BigDecimal.valueOf(100), BigDecimal.valueOf(100),
        2.0, null, null, List.of(), Map.of(), List.of());

    controller.loadGame(save);

    assertEquals(2.0, settings.getDifficulty(), 1e-9);
  }

  @Test
  void loadGameUsesOnGameLoadConsumerWhenSet() {
    AtomicReference<PlayerController> loaded = new AtomicReference<>();
    controller.setOnGameLoadConsumer((ex, pl) -> loaded.set(pl));

    SaveData save = new SaveData(
        "LoadOnly", BigDecimal.valueOf(50), BigDecimal.valueOf(50),
        null, null, null, List.of(), Map.of(), List.of());

    controller.loadGame(save);

    assertNotNull(loaded.get());
    // The start consumer must NOT be invoked when a dedicated load consumer is set.
    assertEquals(null, capturedPlayer.get());
  }

  @Test
  void loadGameSkipsTransactionsForUnknownStocks() {
    SaveData save = new SaveData(
        "Skipper", BigDecimal.valueOf(100), BigDecimal.valueOf(100),
        null, null, null, List.of(),
        Map.of(),
        List.of(new SaveData.TxEntry(
            "BUY", "GHOST", BigDecimal.ONE, BigDecimal.ONE, 1, Instant.now(), null, null)));

    controller.loadGame(save);

    assertTrue(capturedPlayer.get().getTransactionArchive().isEmpty());
  }

  @Test
  void loadGameRejectsNullSave() {
    assertThrows(NullPointerException.class, () -> controller.loadGame(null));
  }

  @Test
  void constructorRejectsNullExchange() {
    assertThrows(NullPointerException.class,
        () -> new MainMenuController(null, (ex, pl) -> {}));
  }
}
