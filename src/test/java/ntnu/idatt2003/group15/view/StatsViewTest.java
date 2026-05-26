package ntnu.idatt2003.group15.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import ntnu.idatt2003.group15.model.stocks.Share;
import ntnu.idatt2003.group15.model.stocks.Stock;
import ntnu.idatt2003.group15.model.stocks.StockSectors;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class StatsViewTest {

  @BeforeAll
  static void initJavaFx() {
    JavaFxTestSupport.ensureStarted();
  }

  @Test
  void constructorBuildsViewWithoutThrowing() {
    JavaFxTestSupport.runAndWait(() -> {
      StatsView view = new StatsView();
      assertNotNull(view.getHoldings());
      assertTrue(view.getHoldings().isEmpty());
    });
  }

  @Test
  void setHoldingsPushesIntoObservableList() {
    JavaFxTestSupport.runAndWait(() -> {
      StatsView view = new StatsView();
      Stock stock = new Stock("AAPL", "Apple", BigDecimal.valueOf(100), 0.0, 0.0,
          List.of(StockSectors.TECHNOLOGY));
      Share share = new Share(stock, BigDecimal.valueOf(5), BigDecimal.valueOf(100));

      view.setHoldings(List.of(share));

      assertEquals(1, view.getHoldings().size());
      assertEquals(share, view.getHoldings().getFirst());
    });
  }

  @Test
  void settersAcceptValuesWithoutThrowing() {
    JavaFxTestSupport.runAndWait(() -> {
      StatsView view = new StatsView();
      // Exercise every public setter — drives line coverage on the view's update paths.
      view.setTotalTrades(10, 6, 4);
      view.setRealizedPl("+$50.00", StatsView.Tone.POSITIVE);
      view.setUnrealizedPl("-$10.00", StatsView.Tone.NEGATIVE);
      view.setWinRate("60.0%", 3, 2);
      view.setTotalReturn("+$40.00", "(+4.00%)", StatsView.Tone.POSITIVE);
      view.setAvgTradeSize("$5.00");
      view.setMostTraded("AAPL");
      // If we got here, no setter threw.
      assertNotNull(view.getHoldings());
    });
  }
}
