package ntnu.idatt2003.group15.model.factories;

import ntnu.idatt2003.group15.model.stocks.Share;
import ntnu.idatt2003.group15.model.transactions.Purchase;
import ntnu.idatt2003.group15.model.transactions.Sale;
import ntnu.idatt2003.group15.model.transactions.Transaction;

/**
 * Factory class for creating instances of {@link Transaction}.
 * This class centralizes the logic for instantiating different types of transactions,
 * such as purchases and sales, ensuring a decoupled architecture.
 */
public class TransactionFactory {

  /**
   * Creates a new Transaction object based on the specified type.
   *
   * @param type the type of transaction to create
   * @param share the share involved in the transaction
   * @param week the week number the transaction occurs in
   * @return a concrete implementation of the {@link Transaction} interface
   * @throws IllegalArgumentException if the provided transaction type is not supported
   */
  public static Transaction createTransaction(TransactionType type, Share share, int week) {
    return switch (type) {
      case PURCHASE -> new Purchase(share, week);
      case SALE -> new Sale(share, week);
      default -> throw new IllegalArgumentException("Illegal transaction type");
    };
  }
}
