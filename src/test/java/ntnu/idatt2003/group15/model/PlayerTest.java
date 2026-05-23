package ntnu.idatt2003.group15.model;

import ntnu.idatt2003.group15.model.exceptions.BlankArgumentException;
import ntnu.idatt2003.group15.model.player.Player;
import ntnu.idatt2003.group15.model.player.PlayerStatus;
import ntnu.idatt2003.group15.model.stocks.Share;
import ntnu.idatt2003.group15.model.stocks.Stock;
import ntnu.idatt2003.group15.model.stocks.StockSectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PlayerTest {

  @Nested
  @DisplayName("Positive Player Tests")
  class positivePlayerTests {
    private Player player;

    @BeforeEach
    void setUp() {
      player = new Player("username", BigDecimal.valueOf(1000));
    }

    @Test
    void getName() {
      assertEquals("username", player.getName());
    }

    @Test
    void getMoney() {
      assertEquals(BigDecimal.valueOf(1000), player.getMoney());
    }

    @Test
    void addMoney() {
      player.addMoney(BigDecimal.valueOf(200));
      assertEquals(BigDecimal.valueOf(1200), player.getMoney());
    }

    @Test
    void withdrawMoney() {
      player.withdrawMoney(BigDecimal.valueOf(200));
      assertEquals(BigDecimal.valueOf(800), player.getMoney());
    }

    @Test
    void getPortfolioIsNotNull() {
      assertNotNull(player.getPortfolio());
    }

    @Test
    void getNetWorth() {
      assertEquals(BigDecimal.valueOf(1000), player.getNetWorth());
    }

    @Test
    void cashPropertyReflectsMoney() {
      assertEquals(BigDecimal.valueOf(1000), player.getCashProperty().getValue());
      player.addMoney(BigDecimal.valueOf(250));
      assertEquals(BigDecimal.valueOf(1250), player.getCashProperty().getValue());
    }

    @Test
    void netWorthPropertyEqualsStartingMoneyWithEmptyPortfolio() {
      assertEquals(BigDecimal.valueOf(1000), player.getNetWorthProperty().getValue());
    }

    @Test
    void netWorthPropertyIncludesPortfolioMarketValue() {
      Stock stock = new Stock("AAPL", "Apple", BigDecimal.valueOf(10), 0.0, 0.0,
          List.of(StockSectors.TECHNOLOGY));
      Share share = new Share(stock, BigDecimal.valueOf(50), stock.getSalesPrice());
      player.getPortfolio().addShare(share);
      // 1000 cash + 50 * 10 market value = 1500
      assertEquals(0,
          BigDecimal.valueOf(1500).compareTo(player.getNetWorthProperty().getValue()));
    }

    @Test
    void netWorthPropertyReactsToMoneyChanges() {
      player.withdrawMoney(BigDecimal.valueOf(400));
      assertEquals(BigDecimal.valueOf(600), player.getNetWorthProperty().getValue());
    }

    @Test
    void netWorthChangeIsZeroAtStart() {
      assertEquals(0, BigDecimal.ZERO.compareTo(player.getNetWorthChangeProperty().getValue()));
    }

    @Test
    void netWorthChangeReflectsGains() {
      player.addMoney(BigDecimal.valueOf(500));
      assertEquals(0,
          BigDecimal.valueOf(500).compareTo(player.getNetWorthChangeProperty().getValue()));
    }

    @Test
    void netWorthChangePercentIsZeroAtStart() {
      assertEquals(0,
          BigDecimal.ZERO.compareTo(player.getNetWorthChangePercentProperty().getValue()));
    }

    @Test
    void netWorthChangePercentReflectsGains() {
      player.addMoney(BigDecimal.valueOf(250)); // +25% on 1000
      assertEquals(0,
          new BigDecimal("25.00")
              .compareTo(player.getNetWorthChangePercentProperty().getValue()));
    }

    @Test
    void netWorthChangePercentIsZeroWhenStartingMoneyZero() {
      Player broke = new Player("broke", BigDecimal.ZERO);
      broke.addMoney(BigDecimal.valueOf(100));
      assertEquals(0,
          BigDecimal.ZERO.compareTo(broke.getNetWorthChangePercentProperty().getValue()));
    }

    @Test
    void getStartingMoneyReturnsInitialBalance() {
      assertEquals(0, BigDecimal.valueOf(1000).compareTo(player.getStartingMoney()));
    }

    @Test
    void getTransactionArchiveReturnsEmptyArchive() {
      assertNotNull(player.getTransactionArchive());
      assertTrue(player.getTransactionArchive().isEmpty());
    }

    @Test
    void moneyPropertyReturnsLiveProperty() {
      assertEquals(0, BigDecimal.valueOf(1000).compareTo(player.moneyProperty().get()));
      player.addMoney(BigDecimal.valueOf(50));
      assertEquals(0, BigDecimal.valueOf(1050).compareTo(player.moneyProperty().get()));
    }

    @Test
    void statusPropertyDefaultsToNovice() {
      assertEquals(PlayerStatus.NOVICE, player.statusProperty().getValue());
    }
  }

  @Nested
  @DisplayName("Negative Player Tests")
  class negativePlayerTests {

    @Test
    void nullName() {
      assertThrows(NullPointerException.class, () ->
          new Player(null, BigDecimal.valueOf(1000))
      );
    }

    @Test
    void blankName() {
      assertThrows(BlankArgumentException.class, () ->
          new Player("   ", BigDecimal.valueOf(1000))
      );
    }

    @Test
    void nullStartingMoney() {
      assertThrows(NullPointerException.class, () ->
          new Player("username", null), "StartingMoney cannot be null"
      );
    }

    @Test
    void nullAmountAddMoney() {
      Player player = new Player("username", BigDecimal.valueOf(1000));
      assertThrows(NullPointerException.class, () ->
          player.addMoney(null)
      );
    }

    @Test
    void nullAmountWithdrawMoney() {
      Player player = new Player("username", BigDecimal.valueOf(1000));
      assertThrows(NullPointerException.class, () ->
          player.withdrawMoney(null)
      );
    }
  }
}
