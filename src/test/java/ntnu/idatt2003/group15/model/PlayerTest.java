package ntnu.idatt2003.group15.model;

import ntnu.idatt2003.group15.model.exceptions.BlankArgumentException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

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
          new Player("username", null)
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
