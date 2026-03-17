package ntnu.idatt2003.group15.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class PlayerTest {
  private Player player;

  @BeforeEach
  void setUp() {
    player = new Player("username", BigDecimal.valueOf(1000));
  }

  @Test
  void getName() {
    String expectedUsername = "username";
    assertEquals(expectedUsername, player.getName());
  }

  @Test
  void getMoney() {
    BigDecimal expectedMoney = BigDecimal.valueOf(1000);
    assertEquals(expectedMoney, player.getMoney());
  }

  @Test
  void addMoney() {
    BigDecimal expectedMoney = BigDecimal.valueOf(1200);
    player.addMoney(BigDecimal.valueOf(200));
    assertEquals(expectedMoney, player.getMoney());
  }

  @Test
  void withdrawMoney() {
    BigDecimal expectedMoney = BigDecimal.valueOf(800);
    player.withdrawMoney(BigDecimal.valueOf(200));
    assertEquals(expectedMoney, player.getMoney());
  }
}