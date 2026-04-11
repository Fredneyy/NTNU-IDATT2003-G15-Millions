package ntnu.idatt2003.group15.model.factories;

import ntnu.idatt2003.group15.model.Purchase;
import ntnu.idatt2003.group15.model.Sale;
import ntnu.idatt2003.group15.model.Share;
import ntnu.idatt2003.group15.model.Transaction;

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
        switch (type) {
            case PURCHASE:
                return new Purchase(share, week);
            case SALE:
                return new Sale(share, week);
            default:
                throw new IllegalArgumentException("Illegal transaction type");
        }
    }
}
