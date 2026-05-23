package ntnu.idatt2003.group15.view.dialog;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.value.ObservableValue;
import javafx.scene.layout.*;
import ntnu.idatt2003.group15.controller.PortfolioController;
import ntnu.idatt2003.group15.model.transactions.SaleCalculator;
import ntnu.idatt2003.group15.model.stocks.Share;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class SellStockDialog extends TransactionDialog {

  private final SaleCalculator calculator;
  private final BigDecimal commissionRate;
  private final BigDecimal taxRate;
  private final BiConsumer<Share, BigDecimal> onConfirm;
  private final Consumer<Throwable> errorHandler;

  public SellStockDialog(ObservableValue<BigDecimal> cashProperty,
                         PortfolioController portfolioController,
                         SaleCalculator calculator,
                         BigDecimal commissionRate,
                         BigDecimal taxRate,
                         BiConsumer<Share, BigDecimal> onConfirm,
                         Consumer<Throwable> errorHandler) {
    super(cashProperty);
    this.onConfirm = Objects.requireNonNull(onConfirm);
    this.calculator = Objects.requireNonNull(calculator, "calculator cannot be null");
    this.commissionRate = Objects.requireNonNull(commissionRate, "commissionRate cannot be null");
    this.taxRate = Objects.requireNonNull(taxRate, "taxRate cannot be null");
    this.errorHandler = Objects.requireNonNull(errorHandler, "errorHandler cannot be null");
    assembleBody();
    summaryTextLabel1.setText("You Receive");
    summaryTextLabel2.setText("Fees and Taxes");
    summaryTextLabel3.setText("Gross Proceeds");
  }

  public void show(StackPane root, Share share) {
    this.root = Objects.requireNonNull(root);
    bindToShare(share);
    if (!root.getChildren().contains(dialog)) {
      dialog.setOpacity(0);
      dialog.setScaleX(0.1);
      dialog.setScaleY(0.1);
      root.getChildren().add(dialog);
      openAnimation.play();
    }
  }

  private void bindToShare(Share share) {
    stockSymbol.setText("Sell " + share.stock().getSymbol());
    stockName.setText(share.stock().getCompany());
    transactionButton.setText("$ Sell");
    quantityField.setText("");
    qtyValue.set(BigDecimal.ZERO);

    ObservableValue<BigDecimal> price = share.stock().getPriceBinding();

    currentPrice.textProperty().unbind();
    currentPrice.textProperty().bind(Bindings.createStringBinding(
        () -> formatMoney(price.getValue()), price));

    amountLabel.setText("Owned: %s".formatted(share.quantity().toPlainString()));

    ObjectBinding<BigDecimal> gross = Bindings.createObjectBinding(() -> {
          if (qtyValue.get().compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
          } else {
            BigDecimal number = calculator.calculateGross(new Share(share.stock(), qtyValue.get().min(share.quantity()), share.pricePerShare()));
            return Objects.requireNonNullElse(number, BigDecimal.ZERO);
          }
        }
        , share.stock().getPriceBinding(), qtyValue);

    ObjectBinding<BigDecimal> net = Bindings.createObjectBinding(() -> {
          if (qtyValue.get().compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
          } else {
            BigDecimal number = calculator.calculateTotal(
                new Share(share.stock(),  qtyValue.get().min(share.quantity()), share.pricePerShare()), commissionRate, taxRate);
            return Objects.requireNonNullElse(number, BigDecimal.ZERO);
          }
        }
    , share.stock().getPriceBinding(), qtyValue);

    ObjectBinding<BigDecimal> fee = Bindings.createObjectBinding(
        () -> gross.get().subtract(net.get()), share.stock().getPriceBinding(), qtyValue);

    maxButton.setOnAction(_ -> {
      BigDecimal m = share.quantity();
      quantityField.setText(m.toPlainString());
    });

    summaryLabel3.textProperty().unbind();
    summaryLabel3.textProperty().bind(Bindings.createStringBinding(
        () -> formatMoney(gross.get()), gross));

    summaryLabel2.textProperty().unbind();
    summaryLabel2.textProperty().bind(Bindings.createStringBinding(
        () -> formatMoney(fee.get()), fee));

    summaryLabel1.textProperty().unbind();
    summaryLabel1.textProperty().bind(Bindings.createStringBinding(
        () -> formatMoney(net.get()), net));

    BooleanBinding cannotBuy = Bindings.createBooleanBinding(() -> {
      BigDecimal q = qtyValue.get();
      BigDecimal shareQ = share.quantity();
      return q == null || q.signum() <= 0 || q.compareTo(shareQ) > 0;
    }, qtyValue);

    transactionButton.disableProperty().unbind();
    transactionButton.disableProperty().bind(cannotBuy);

    transactionButton.setOnAction(_ -> {
      BigDecimal q = qtyValue.get();
      if (q == null || q.signum() <= 0) {
        return;
      }
      try {
        onConfirm.accept(share, q);
        close();
      } catch (RuntimeException ex) {
        errorHandler.accept(ex);
      }
    });
  }
}
