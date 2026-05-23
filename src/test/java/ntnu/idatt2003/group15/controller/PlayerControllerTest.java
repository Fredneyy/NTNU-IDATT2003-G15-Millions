package ntnu.idatt2003.group15.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import ntnu.idatt2003.group15.model.player.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PlayerControllerTest {

  private Player player;
  private PlayerController controller;

  @BeforeEach
  void setUp() {
    player = new Player("Alice", BigDecimal.valueOf(1000));
    controller = new PlayerController(player);
  }

  @Test
  void getPlayerReturnsBackingPlayer() {
    assertSame(player, controller.getPlayer());
  }

  @Test
  void getNameDelegatesToPlayer() {
    assertEquals("Alice", controller.getName());
  }

  @Test
  void getMoneyReflectsLiveBalance() {
    player.addMoney(BigDecimal.valueOf(500));
    assertEquals(0, BigDecimal.valueOf(1500).compareTo(controller.getMoney()));
  }

  @Test
  void getStartingMoneyExposesInitialBalance() {
    assertEquals(0, BigDecimal.valueOf(1000).compareTo(controller.getStartingMoney()));
  }

  @Test
  void getPortfolioReturnsBackingPortfolio() {
    assertSame(player.getPortfolio(), controller.getPortfolio());
  }

  @Test
  void getTransactionArchiveReturnsBackingArchive() {
    assertSame(player.getTransactionArchive(), controller.getTransactionArchive());
  }

  @Test
  void getNetWorthEqualsStartingMoneyForEmptyPortfolio() {
    assertEquals(0, BigDecimal.valueOf(1000).compareTo(controller.getNetWorth()));
  }

  @Test
  void cashPropertyTracksMoneyChanges() {
    assertEquals(0,
        BigDecimal.valueOf(1000).compareTo(controller.getCashProperty().getValue()));
    player.addMoney(BigDecimal.valueOf(250));
    assertEquals(0,
        BigDecimal.valueOf(1250).compareTo(controller.getCashProperty().getValue()));
  }

  @Test
  void netWorthPropertyTracksMoneyChanges() {
    player.addMoney(BigDecimal.valueOf(100));
    assertEquals(0,
        BigDecimal.valueOf(1100).compareTo(controller.getNetWorthProperty().getValue()));
  }

  @Test
  void statusPropertyIsNotNull() {
    assertNotNull(controller.statusProperty().getValue());
    assertEquals(controller.statusProperty().getValue(), controller.getStatus());
  }

  @Test
  void moneyPropertyTracksMoneyChanges() {
    assertEquals(0, BigDecimal.valueOf(1000).compareTo(controller.moneyProperty().getValue()));
    player.addMoney(BigDecimal.valueOf(200));
    assertEquals(0, BigDecimal.valueOf(1200).compareTo(controller.moneyProperty().getValue()));
  }

  @Test
  void netWorthChangePropertyIsZeroAtStart() {
    assertEquals(0,
        BigDecimal.ZERO.compareTo(controller.getNetWorthChangeProperty().getValue()));
  }

  @Test
  void netWorthChangePropertyReflectsGains() {
    player.addMoney(BigDecimal.valueOf(300));
    assertEquals(0,
        BigDecimal.valueOf(300).compareTo(controller.getNetWorthChangeProperty().getValue()));
  }

  @Test
  void netWorthChangePercentPropertyIsZeroAtStart() {
    assertEquals(0,
        BigDecimal.ZERO.compareTo(controller.getNetWorthChangePercentProperty().getValue()));
  }

  @Test
  void netWorthChangePercentPropertyReflectsGains() {
    player.addMoney(BigDecimal.valueOf(250)); // +25% on 1000
    assertEquals(0,
        new BigDecimal("25.00")
            .compareTo(controller.getNetWorthChangePercentProperty().getValue()));
  }

  @Test
  void constructorRejectsNullPlayer() {
    assertThrows(NullPointerException.class, () -> new PlayerController(null));
  }
}
