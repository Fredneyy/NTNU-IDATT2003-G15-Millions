package ntnu.idatt2003.group15.model.stocks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class StockSectorsTest {

  @Test
  void fromLabelMatchesExactLabel() {
    assertEquals(StockSectors.TECHNOLOGY, StockSectors.fromLabel("TECHNOLOGY"));
  }

  @Test
  void fromLabelIsCaseInsensitive() {
    assertEquals(StockSectors.ENERGY, StockSectors.fromLabel("energy"));
    assertEquals(StockSectors.REALESTATE, StockSectors.fromLabel("RealEstate"));
  }

  @Test
  void getLabelReturnsTheBackingString() {
    assertEquals("FINANCIALS", StockSectors.FINANCIALS.getLabel());
  }

  @Test
  void fromUnknownLabelThrows() {
    assertThrows(IllegalArgumentException.class, () -> StockSectors.fromLabel("crypto"));
  }

  @Test
  void fromNullLabelThrows() {
    // equalsIgnoreCase(null) returns false, so no match is found and the
    // orElseThrow branch fires.
    assertThrows(IllegalArgumentException.class, () -> StockSectors.fromLabel(null));
  }
}
