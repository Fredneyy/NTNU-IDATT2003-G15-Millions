package ntnu.idatt2003.group15.model;

/**
 * A price event for updating a stocks price.
 *
 * <p>Implements price event for ensuring stock simulator has
 * needed data</p>
 */
public record StandardPriceEvent(double drift, double volatility)
  implements PriceEvent {}
