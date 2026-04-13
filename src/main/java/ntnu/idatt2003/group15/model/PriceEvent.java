package ntnu.idatt2003.group15.model;

/**
 * The interface Price event.
 *
 * <p>Ensures the stock simulator has the right information to update
 * a return a new stock price</p>
 */
public interface PriceEvent {
  /**
   * Drift, the amount of drift in the calculations
   *
   * @return the double
   */
  double drift();

  /**
   * Volatility, the amount of volatility in the calculations
   *
   * @return the double
   */
  double volatility();
}
