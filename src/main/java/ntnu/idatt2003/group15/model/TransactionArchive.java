package ntnu.idatt2003.group15.model;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class TransactionArchive {
    private List<Transaction> transactions;

    public TransactionArchive() {
        transactions = new java.util.ArrayList<>();
    }

    public boolean add(Transaction transaction) {
        return transactions.add(transaction);
    }

    public boolean isEmpty() {
        return transactions.isEmpty();
    }

    public List<Transaction> getTransactions(int week) {
        return transactions.stream().filter(transaction -> transaction.getWeek() == week).toList();
    }

    public List<Purchase> getPurchases(int week) {
        return transactions.stream().filter(Purchase.class::isInstance).filter(transaction -> transaction.getWeek() == week).map(Purchase.class::cast).toList();
    }

    public List<Sale> getSales(int week) {
        return transactions.stream().filter(Sale.class::isInstance).filter(transaction -> transaction.getWeek() == week).map(Sale.class::cast).toList();
    }

    public int countDistinctWeeks() {
        Set<Integer> distinctWeeks = transactions.stream()
                .map(Transaction::getWeek) // Get the week numbers
                .collect(Collectors.toSet()); // Collect into a Set for uniqueness

        return distinctWeeks.size();
    }
}
