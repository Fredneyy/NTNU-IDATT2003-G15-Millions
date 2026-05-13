package ntnu.idatt2003.group15.view;

import javafx.animation.ParallelTransition;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import ntnu.idatt2003.group15.model.Stock;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.materialdesign2.MaterialDesignC;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import java.util.function.BiConsumer;

/**
 * Buy-stock popup. Dumb view: receives a cash observable and an {@code onConfirm}
 * callback at construction; rebinds its money/quantity labels each time
 * {@link #show(StackPane, Stock)} is called so prices stay live for the selected stock.
 */
public class BuyStockDialog extends BaseDialog {

  private final Label stockSymbol = new Label();
  private final Label stockName = new Label();
  private final HBox header = new HBox();
  private final Button closeButton = new Button();
  private final Label currentPrice = new Label();
  private final Label maxBuyStockAmountLabel = new Label("Max: 0");
  private final Label totalCost = new Label();
  private final Label availableCash = new Label();
  private final Label remainingCash = new Label();
  private final TextField quantity = new TextField();
  private final Button maxBuyButton = new Button("Max");
  private final Button buyButton = new Button();
  private final ParallelTransition closeAnimation;
  private final ParallelTransition openAnimation;

  private final ObservableValue<BigDecimal> cashProperty;
  private final BiConsumer<Stock, BigDecimal> onConfirm;
  private final ObjectProperty<BigDecimal> qtyValue = new SimpleObjectProperty<>(BigDecimal.ZERO);

  private ChangeListener<BigDecimal> remainingSignListener;
  private ObservableValue<BigDecimal> remainingObservable;

  public BuyStockDialog(ObservableValue<BigDecimal> cashProperty,
                        BiConsumer<Stock, BigDecimal> onConfirm) {
    super();
    this.cashProperty = Objects.requireNonNull(cashProperty, "cashProperty cannot be null");
    this.onConfirm = Objects.requireNonNull(onConfirm, "onConfirm cannot be null");

    dialog.setMaxHeight((int) Screen.getPrimary().getVisualBounds().getHeight() / 2.5);
    dialog.setMaxWidth((int) Screen.getPrimary().getVisualBounds().getWidth() / 5.0);
    dialog.getStyleClass().setAll("stock-dialog-card", "buy-stock-dialog");

    VBox body = new VBox(buildHeader(), buildBuyMenu());
    body.getStyleClass().add("buy-dialog-body");
    dialog.getChildren().add(body);

    quantity.textProperty().addListener((_, _, nv) -> qtyValue.set(parseQuantity(nv)));

    availableCash.textProperty().bind(Bindings.createStringBinding(
        () -> formatMoney(cashProperty.getValue()), cashProperty));

    closeAnimation = createCloseAnimation(_ -> root.getChildren().remove(dialog));
    openAnimation = createOpenAnimation();
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

  @Override
  public void show(StackPane root) {
    this.root = Objects.requireNonNull(root);
    if (!root.getChildren().contains(dialog)) {
      root.getChildren().add(dialog);
    }
  }

  @Override
  public void close() {
    if (root != null) {
      closeAnimation.play();
    }
  }

  private void bindToStock(Stock stock) {
    stockSymbol.setText("Buy " + stock.getSymbol());
    stockName.setText(stock.getCompany());
    buyButton.setText("$ Buy");
    quantity.setText("");
    qtyValue.set(BigDecimal.ZERO);

    ObservableValue<BigDecimal> price = stock.getPriceBinding();

    currentPrice.textProperty().unbind();
    currentPrice.textProperty().bind(Bindings.createStringBinding(
        () -> formatMoney(price.getValue()), price));

    ObjectBinding<BigDecimal> maxBuyable = Bindings.createObjectBinding(() -> {
      BigDecimal cash = cashProperty.getValue();
      BigDecimal p = price.getValue();
      if (cash == null || p == null || p.signum() == 0) return BigDecimal.ZERO;
      return cash.divide(p, 0, RoundingMode.DOWN).max(BigDecimal.ZERO);
    }, cashProperty, price);

    maxBuyStockAmountLabel.textProperty().unbind();
    maxBuyStockAmountLabel.textProperty().bind(Bindings.createStringBinding(
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

    remainingCash.textProperty().unbind();
    remainingCash.textProperty().bind(Bindings.createStringBinding(
        () -> formatMoney(remainingValue.get()), remainingValue));

    BooleanBinding cannotBuy = Bindings.createBooleanBinding(() -> {
      BigDecimal q = qtyValue.get();
      BigDecimal cost = totalCostValue.get();
      BigDecimal cash = cashProperty.getValue();
      return q == null || q.signum() <= 0 || cash == null || cost.compareTo(cash) > 0;
    }, qtyValue, totalCostValue, cashProperty);

    buyButton.disableProperty().unbind();
    buyButton.disableProperty().bind(cannotBuy);

    buyButton.setOnAction(_ -> {
      BigDecimal q = qtyValue.get();
      if (q == null || q.signum() <= 0) return;
      onConfirm.accept(stock, q);
      close();
    });

    maxBuyButton.setOnAction(_ -> {
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
    remainingCash.getStyleClass().removeAll("value-positive", "value-negative");
    if (v == null) return;
    remainingCash.getStyleClass().add(v.signum() >= 0 ? "value-positive" : "value-negative");
  }

  private static BigDecimal parseQuantity(String text) {
    if (text == null || text.isBlank()) return BigDecimal.ZERO;
    try {
      BigDecimal v = new BigDecimal(text.trim());
      return v.signum() < 0 ? BigDecimal.ZERO : v;
    } catch (NumberFormatException e) {
      return BigDecimal.ZERO;
    }
  }

  private static String formatMoney(BigDecimal v) {
    if (v == null) return "$0.00";
    String sign = v.signum() < 0 ? "-" : "";
    return sign + "$" + v.abs().setScale(2, RoundingMode.HALF_UP).toPlainString();
  }

  private VBox buildBuyMenu() {
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

    maxBuyButton.getStyleClass().add("buy-max-button");

    HBox inputHBox = new HBox(quantity, maxBuyButton);
    inputHBox.getStyleClass().add("buy-quantity-row");

    maxBuyStockAmountLabel.getStyleClass().add("buy-max-hint");

    VBox quantityContainer = new VBox(quantityLabel, inputHBox, maxBuyStockAmountLabel);
    quantityContainer.getStyleClass().add("buy-quantity-container");

    HBox costHbox = buildCostBox();

    buyButton.getStyleClass().add("buy-confirm-button");
    buyButton.setMaxWidth(Double.MAX_VALUE);

    VBox container = new VBox(priceContainer, quantityContainer, costHbox, buyButton);
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
    remainingCash.getStyleClass().add("buy-cost-value");
    VBox costContainer = new VBox(totalCost, availableCash, remainingCash);
    costContainer.getStyleClass().add("buy-cost-values");
    costContainer.setAlignment(Pos.CENTER_RIGHT);

    HBox row = new HBox(costContainerText, spacer, costContainer);
    row.getStyleClass().add("buy-cost-card");
    return row;
  }

  private HBox buildHeader() {
    stockSymbol.getStyleClass().add("buy-header-title");
    stockName.getStyleClass().add("buy-header-subtitle");

    VBox headerVBox = new VBox(stockSymbol, stockName);
    headerVBox.getStyleClass().add("buy-header-text");
    HBox.setHgrow(headerVBox, Priority.ALWAYS);

    closeButton.getStyleClass().setAll("close-button", "buy-close-button");
    closeButton.setOnAction(_ -> close());
    FontIcon closeIcon = FontIcon.of(MaterialDesignC.CLOSE);
    closeIcon.setIconColor(Color.WHITE);
    closeButton.setGraphic(closeIcon);
    closeButton.setAlignment(Pos.CENTER);

    header.getChildren().setAll(headerVBox, closeButton);
    header.getStyleClass().add("buy-header");
    return header;
  }
}
