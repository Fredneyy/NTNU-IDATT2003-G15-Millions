package ntnu.idatt2003.group15.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


class PortfolioTest {
  private Portfolio portfolio;
  private Share share;
  private Stock stock;

  @BeforeEach
  void setUp() {
    stock = new Stock("AAPL", "Apple", BigDecimal.valueOf(10));
    share = new Share(stock, BigDecimal.valueOf(100), stock.getSalesPrice());
    portfolio = new Portfolio();
  }

  @Test
  void addShare() {
    assertTrue(portfolio.addShare(share));
  }

  @Test
  void removeShare() {
    portfolio.addShare(share);
    assertTrue(portfolio.removeShare(share));
  }

  @Test
  void getShares() {
    portfolio.addShare(share);
    assertEquals(share.getStock().getCompany()
        , portfolio.getShares().getFirst().getStock().getCompany());
  }

  @Test
  void testGetShares() {
  }

  @Test
  void contains() {
  }
}