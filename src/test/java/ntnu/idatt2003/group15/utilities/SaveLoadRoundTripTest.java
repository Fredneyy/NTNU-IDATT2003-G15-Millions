package ntnu.idatt2003.group15.utilities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import ntnu.idatt2003.group15.controller.ExchangeController;
import ntnu.idatt2003.group15.controller.PlayerController;
import ntnu.idatt2003.group15.model.Exchange;
import ntnu.idatt2003.group15.model.GameSettings;
import ntnu.idatt2003.group15.model.SaveData;
import ntnu.idatt2003.group15.model.player.Player;
import ntnu.idatt2003.group15.model.stocks.Stock;
import ntnu.idatt2003.group15.model.stocks.StockSectors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SaveLoadRoundTripTest {

  @TempDir
  Path tempDir;

  private String originalUserHome;

  @BeforeEach
  void redirectUserHomeToTempDir() {
    // SaveGameUtil.save() writes to ~/.millions/recent-saves.tsv via SaveIndex.
    // Redirect user.home so the real index is never touched.
    originalUserHome = System.getProperty("user.home");
    System.setProperty("user.home", tempDir.toString());
  }

  @AfterEach
  void restoreUserHome() {
    if (originalUserHome != null) {
      System.setProperty("user.home", originalUserHome);
    } else {
      System.clearProperty("user.home");
    }
  }

  @Test
  void savedFileRoundTripsBackIntoEquivalentSaveData() throws Exception {
    Stock apple = new Stock("AAPL", "Apple", BigDecimal.valueOf(150), 0.0008, 0.22,
        List.of(StockSectors.TECHNOLOGY));
    Stock equinor = new Stock("EQNR", "Equinor", BigDecimal.valueOf(300), 0.0005, 0.15,
        List.of(StockSectors.ENERGY));
    Exchange exchange = new Exchange("FREX", List.of(apple, equinor));
    Player player = new Player("Saver", BigDecimal.valueOf(5000));

    ExchangeController exchangeController = new ExchangeController(exchange);
    PlayerController playerController = new PlayerController(player);
    exchangeController.buy(apple, BigDecimal.valueOf(3), player);

    GameSettings settings = new GameSettings();
    settings.setDifficulty(1.5);

    File target = tempDir.resolve("save.json").toFile();
    SaveGameUtil.save(target, playerController, exchangeController, settings);

    SaveData loaded = LoadGameUtil.load(target);

    assertEquals("Saver", loaded.playerName());
    assertEquals(0, BigDecimal.valueOf(5000 - 3 * 150).compareTo(loaded.cash()));
    assertEquals(0, BigDecimal.valueOf(5000).compareTo(loaded.startingMoney()));
    assertEquals(1.5, loaded.difficulty(), 1e-9);
    assertEquals(1, loaded.week());

    assertEquals(1, loaded.shares().size());
    SaveData.ShareEntry holding = loaded.shares().getFirst();
    assertEquals("AAPL", holding.symbol());
    assertEquals(0, BigDecimal.valueOf(3).compareTo(holding.quantity()));
    assertEquals(0, BigDecimal.valueOf(150).compareTo(holding.pricePerShare()));

    assertEquals(0, BigDecimal.valueOf(150).compareTo(loaded.stockPrices().get("AAPL")));
    assertEquals(0, BigDecimal.valueOf(300).compareTo(loaded.stockPrices().get("EQNR")));

    assertEquals(1, loaded.transactions().size());
    SaveData.TxEntry tx = loaded.transactions().getFirst();
    assertEquals("BUY", tx.type());
    assertEquals("AAPL", tx.symbol());
    assertNotNull(tx.committedAt());
  }

  @Test
  void loadTolerantlySkipsMalformedShareEntries() throws Exception {
    Path file = tempDir.resolve("malformed.json");
    Files.writeString(file, """
        {
          "player": {
            "name": "PartialSaver",
            "cash": 1234.5,
            "portfolio": [
              { "symbol": "AAPL", "quantity": 2, "pricePerShare": 100 },
              { "symbol": "BAD" }
            ]
          },
          "exchange": {
            "name": "FREX",
            "week": 7,
            "stocks": []
          }
        }
        """);

    SaveData data = LoadGameUtil.load(file.toFile());

    // The well-formed entry is kept; the incomplete one is dropped.
    assertEquals(1, data.shares().size());
    assertEquals("AAPL", data.shares().getFirst().symbol());
    assertEquals(7, data.week());
  }

  @Test
  void loadReturnsEmptyCollectionsWhenFieldsMissing() throws Exception {
    Path file = tempDir.resolve("minimal.json");
    Files.writeString(file, """
        {
          "player": { "name": "Lonely", "cash": 100 }
        }
        """);

    SaveData data = LoadGameUtil.load(file.toFile());

    assertEquals("Lonely", data.playerName());
    assertEquals(0, BigDecimal.valueOf(100).compareTo(data.cash()));
    assertNull(data.startingMoney());
    assertNull(data.week());
    // Collections are always present (never null) for caller convenience.
    assertEquals(0, data.shares().size());
    assertEquals(0, data.transactions().size());
  }
}
