package ntnu.idatt2003.group15.model.stocks;

import java.util.Arrays;

/**
 * Represents sectors for stocks.
 */
public enum StockSectors {
  FINANCIALS("FINANCIALS"),
  TECHNOLOGY("TECHNOLOGY"),
  HEALTHCARE("HEALTHCARE"),
  INDUSTRIALS("INDUSTRIALS"),
  CONSUMER("CONSUMER"),
  ENERGY("ENERGY"),
  REALESTATE("REALESTATE"),
  MACRO("MACRO");

  private final String label;

  StockSectors(String label) {
    this.label = label;
  }

  /**
   * Returns the string label associated with this sector.
   *
   * @return the label
   */
  public String getLabel() {
    return label;
  }

  /**
   * Looks up a sector by its label, case-insensitively.
   *
   * @param stringLabel the label to look up
   * @return the matching sector
   * @throws IllegalArgumentException if no sector matches the label
   */
  public static StockSectors fromLabel(String stringLabel) {
    return Arrays.stream(values())
        .filter(sector -> sector.label.equalsIgnoreCase(stringLabel))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("Unknown sector: " + stringLabel));
  }
}