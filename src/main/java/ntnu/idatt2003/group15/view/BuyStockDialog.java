package ntnu.idatt2003.group15.view;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import ntnu.idatt2003.group15.model.Stock;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.function.BiConsumer;

public class BuyStockDialog extends TransactionDialog {

  private final Label maxStockAmountLabel = new Label("Max: 0");
  private final Label totalCost = new Label();
  private final Label availableCash = new Label();
  private final Label cashAfterTransaction = new Label();

  private ChangeListener<BigDecimal> remainingSignListener;
  private ObservableValue<BigDecimal> remainingObservable;

  public BuyStockDialog(ObservableValue<BigDecimal> cashProperty,
                        BiConsumer<Stock, BigDecimal> onConfirm) {
    super(cashProperty, onConfirm);
    availableCash.textProperty().bind(Bindings.createStringBinding(
        () -> formatMoney(cashProperty.getValue()), cashProperty));
    assembleBody();
  }

  @Override
  protected VBox buildMenu() {
    Label labelInfo = new Label("Current Price");
    labelInfo.getStyleClass().add("buy-stat-title");
    currentPrice.getStyleClass().add("buy-stat-price");
    VBox priceContainer = new VBox(labelInfo, currentPrice);
    priceContainer.getStyleClass().add("buy-card");

    Label quantityLabel = new Label("Quantity");
    quantityLabel.getStyleClass().add("buy-section-label");
    quantity.setPromptText("Input amount...");
    quantity.getStyleClass().add("buy-quantity-input");
    HBox.setHgrow(quantity, Priority.ALWAYS);

    maxButton.getStyleClass().add("buy-max-button");

    HBox inputHBox = new HBox(quantity, maxButton);
    inputHBox.getStyleClass().add("buy-quantity-row");

    maxStockAmountLabel.getStyleClass().add("buy-max-hint");

    VBox quantityContainer = new VBox(quantityLabel, inputHBox, maxStockAmountLabel);
    quantityContainer.getStyleClass().add("buy-quantity-container");

    HBox costHbox = buildCostBox();

    transactionButton.getStyleClass().add("buy-confirm-button");
    transactionButton.setMaxWidth(Double.MAX_VALUE);

    VBox container = new VBox(priceContainer, quantityContainer, costHbox, transactionButton);
    container.getStyleClass().add("buy-content");
    return container;
  }

  private HBox buildCostBox() {
    Label costTextLabel = new Label("Total Cost");
    Label availableTextLabel = new Label("Available Cash");
    Label remainingTextLabel = new Label("Remaining");
    costTextLabel.getStyleClass().add("buy-cost-label-major");
    availableTextLabel.getStyleClass().add("buy-cost-label");
    remainingTextLabel.getStyleClass().add("buy-cost-label");
    VBox costContainerText = new VBox(costTextLabel, availableTextLabel, remainingTextLabel);
    costContainerText.getStyleClass().add("buy-cost-labels");

    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);

    totalCost.getStyleClass().addAll("buy-cost-value-major");
    availableCash.getStyleClass().add("buy-cost-value");
    cashAfterTransaction.getStyleClass().add("buy-cost-value");
    VBox costContainer = new VBox(totalCost, availableCash, cashAfterTransaction);
    costContainer.getStyleClass().add("buy-cost-values");
    costContainer.setAlignment(Pos.CENTER_RIGHT);

    HBox row = new HBox(costContainerText, spacer, costContainer);
    row.getStyleClass().add("buy-cost-card");
    return row;
  }

  @Override
  protected void bindToStock(Stock stock) {
    stockSymbol.setText("Buy " + stock.getSymbol());
    stockName.setText(stock.getCompany());
    transactionButton.setText("$ Buy");
    quantity.setText("");
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

    maxStockAmountLabel.textProperty().unbind();
    maxStockAmountLabel.textProperty().bind(Bindings.createStringBinding(
        () -> "Max: " + maxBuyable.get().toPlainString(), maxBuyable));

    ObjectBinding<BigDecimal> totalCostValue = Bindings.createObjectBinding(() -> {
      BigDecimal p = price.getValue();
      BigDecimal q = qtyValue.get();
      if (p == null || q == null) return BigDecimal.ZERO;
      return p.multiply(q);
    }, price, qtyValue);

    totalCost.textProperty().unbind();
    totalCost.textProperty().bind(Bindings.createStringBinding(
        () -> formatMoney(totalCostValue.get()), totalCostValue));

    ObjectBinding<BigDecimal> remainingValue = Bindings.createObjectBinding(() -> {
      BigDecimal cash = cashProperty.getValue();
      if (cash == null) return BigDecimal.ZERO.subtract(totalCostValue.get());
      return cash.subtract(totalCostValue.get());
    }, cashProperty, totalCostValue);

    cashAfterTransaction.textProperty().unbind();
    cashAfterTransaction.textProperty().bind(Bindings.createStringBinding(
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
      if (q == null || q.signum() <= 0) return;
      onConfirm.accept(stock, q);
      close();
    });

    maxButton.setOnAction(_ -> {
      BigDecimal m = maxBuyable.get();
      quantity.setText(m == null ? "0" : m.toPlainString());
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
    cashAfterTransaction.getStyleClass().removeAll("value-positive", "value-negative");
    if (v == null) return;
    cashAfterTransaction.getStyleClass().add(v.signum() >= 0 ? "value-positive" : "value-negative");
  }
}
