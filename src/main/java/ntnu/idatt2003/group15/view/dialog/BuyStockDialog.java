package ntnu.idatt2003.group15.view.dialog;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.layout.*;
import ntnu.idatt2003.group15.model.stocks.Stock;
import ntnu.idatt2003.group15.utilities.InputValidator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class BuyStockDialog extends TransactionDialog {

  private final BiConsumer<Stock, BigDecimal> onConfirm;
  private final Consumer<Throwable> errorHandler;

  private ChangeListener<BigDecimal> remainingSignListener;
  private ObservableValue<BigDecimal> remainingObservable;

  public BuyStockDialog(ObservableValue<BigDecimal> cashProperty,
                        BiConsumer<Stock, BigDecimal> onConfirm,
                        Consumer<Throwable> errorHandler) {
    super(cashProperty);
    this.onConfirm = Objects.requireNonNull(onConfirm);
    this.errorHandler = Objects.requireNonNull(errorHandler);
    summaryLabel2.textProperty().bind(Bindings.createStringBinding(
        () -> formatMoney(cashProperty.getValue()), cashProperty));
    assembleBody();
    summaryTextLabel1.setText("Total Cost");
    summaryTextLabel3.setText("Remaining");
    summaryTextLabel2.setText("Available");
  }

  public void show(StackPane root, Stock stock) {
    this.root = Objects.requireNonNull(root);
    bindToStock(Objects.requireNonNull(stock));
    if (!root.getChildren().contains(dialog)) {
      dialog.setOpacity(0);
      dialog.setScaleX(0.1);
      dialog.setScaleY(0.1);
      root.getChildren().add(dialog);
      openAnimation.play();
    }
  }

  protected void bindToStock(Stock stock) {
    stockSymbol.setText("Buy " + stock.getSymbol());
    stockName.setText(stock.getCompany());
    transactionButton.setText("$ Buy");
    quantityField.setText("");
    qtyValue.set(BigDecimal.ZERO);

    ObservableValue<BigDecimal> price = stock.getPriceBinding();

    currentPrice.textProperty().unbind();
    currentPrice.textProperty().bind(Bindings.createStringBinding(
        () -> formatMoney(price.getValue()), price));

    ObjectBinding<BigDecimal> maxBuyable = Bindings.createObjectBinding(() -> {
      BigDecimal cash = cashProperty.getValue();
      BigDecimal p = price.getValue();
      if (cash == null || p == null || p.signum() == 0) {
        return BigDecimal.ZERO;
      }
      return cash.divide(p, 0, RoundingMode.DOWN).max(BigDecimal.ZERO);
    }, cashProperty, price);

    amountLabel.textProperty().unbind();
    amountLabel.textProperty().bind(Bindings.createStringBinding(
        () -> "Max: " + maxBuyable.get().toPlainString(), maxBuyable));

    ObjectBinding<BigDecimal> totalCostValue = Bindings.createObjectBinding(() -> {
      BigDecimal p = price.getValue();
      BigDecimal q = qtyValue.get();
      if (p == null || q == null) return BigDecimal.ZERO;
      return p.multiply(q);
    }, price, qtyValue);

    summaryLabel1.textProperty().unbind();
    summaryLabel1.textProperty().bind(Bindings.createStringBinding(
        () -> formatMoney(totalCostValue.get()), totalCostValue));

    ObjectBinding<BigDecimal> remainingValue = Bindings.createObjectBinding(() -> {
      BigDecimal cash = cashProperty.getValue();
      if (cash == null) return BigDecimal.ZERO.subtract(totalCostValue.get());
      return cash.subtract(totalCostValue.get());
    }, cashProperty, totalCostValue);

    summaryLabel3.textProperty().unbind();
    summaryLabel3.textProperty().bind(Bindings.createStringBinding(
        () -> formatMoney(remainingValue.get()), remainingValue));

    BooleanBinding cannotBuy = Bindings.createBooleanBinding(() -> {
      BigDecimal q = qtyValue.get();
      BigDecimal cost = totalCostValue.get();
      BigDecimal cash = cashProperty.getValue();
      return q == null || q.signum() <= 0 || cash == null || cost.compareTo(cash) > 0;
    }, qtyValue, totalCostValue, cashProperty);

    transactionButton.disableProperty().unbind();
    transactionButton.disableProperty().bind(cannotBuy);

    transactionButton.setOnAction(_ -> {
      BigDecimal q = qtyValue.get();
      if (!InputValidator.isBigDecimalValuePositive(q)) {
        return;
      }
      try {
        onConfirm.accept(stock, q);
        close();
      } catch (RuntimeException ex) {
        errorHandler.accept(ex);
      }
    });

    maxButton.setOnAction(_ -> {
      BigDecimal m = maxBuyable.get();
      quantityField.setText(m == null ? "0" : m.toPlainString());
    });

    if (remainingObservable != null && remainingSignListener != null) {
      remainingObservable.removeListener(remainingSignListener);
    }
    remainingObservable = remainingValue;
    remainingSignListener = (_, _, nv) -> updateRemainingStyle(nv);
    remainingValue.addListener(remainingSignListener);
    updateRemainingStyle(remainingValue.get());
  }

  private void updateRemainingStyle(BigDecimal v) {
    summaryLabel3.getStyleClass().removeAll("value-positive", "value-negative");
    if (v == null) return;
    summaryLabel3.getStyleClass().add(v.signum() >= 0 ? "value-positive" : "value-negative");
  }
}
