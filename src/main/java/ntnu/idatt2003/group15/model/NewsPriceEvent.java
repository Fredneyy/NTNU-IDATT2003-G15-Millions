package ntnu.idatt2003.group15.model;

/**
 * A news price event.
 *
 * <p>Implements price event, is the same as a standard price event,
 * but extends with a message for the displayed news, and a sector
 * for the affected stocks</p>
 */
public record NewsPriceEvent(String message, String sector, double drift, double volatility)
  implements PriceEvent {}
