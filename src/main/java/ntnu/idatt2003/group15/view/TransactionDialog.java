package ntnu.idatt2003.group15.view;

import javafx.animation.ParallelTransition;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.materialdesign2.MaterialDesignC;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public abstract class TransactionDialog extends BaseDialog {

  protected final Label stockSymbol = new Label();
  protected final Label stockName = new Label();
  protected final HBox header = new HBox();
  protected final Button closeButton = new Button();
  protected final Label currentPrice = new Label();
  protected final TextField quantityField = new TextField();
  protected final Button maxButton = new Button("Max");
  protected final Button transactionButton = new Button();

  protected final ObservableValue<BigDecimal> cashProperty;
  protected final ObjectProperty<BigDecimal> qtyValue = new SimpleObjectProperty<>(BigDecimal.ZERO);

  protected ParallelTransition closeAnimation;
  protected ParallelTransition openAnimation;

  protected TransactionDialog(ObservableValue<BigDecimal> cashProperty) {
    super();
    this.cashProperty = Objects.requireNonNull(cashProperty, "cashProperty cannot be null");

    dialog.setMaxHeight((int) Screen.getPrimary().getVisualBounds().getHeight() / 2.5);
    dialog.setMaxWidth((int) Screen.getPrimary().getVisualBounds().getWidth() / 5.0);
    dialog.getStyleClass().setAll("stock-dialog-card", "buy-stock-dialog");

    closeAnimation = createCloseAnimation(_ -> root.getChildren().remove(dialog));
    openAnimation = createOpenAnimation();
  }

  /** Subclasses must call this from their constructor after their own fields are initialized. */
  protected final void assembleBody() {
    VBox body = new VBox(buildHeader(), buildMenu());
    body.getStyleClass().add("buy-dialog-body");
    dialog.getChildren().add(body);
  }

  protected abstract VBox buildMenu();

  public void show(StackPane root) {
    this.root = Objects.requireNonNull(root);
    if (!root.getChildren().contains(dialog)) {
      root.getChildren().add(dialog);
    }
  }

  public void close() {
    if (root != null) {
      closeAnimation.play();
    }
  }

  protected static BigDecimal parseQuantity(String text) {
    if (text == null || text.isBlank()) return BigDecimal.ZERO;
    try {
      BigDecimal v = new BigDecimal(text.trim());
      return v.signum() < 0 ? BigDecimal.ZERO : v;
    } catch (NumberFormatException e) {
      return BigDecimal.ZERO;
    }
  }

  protected static String formatMoney(BigDecimal v) {
    if (v == null) return "$0.00";
    String sign = v.signum() < 0 ? "-" : "";
    return sign + "$" + v.abs().setScale(2, RoundingMode.HALF_UP).toPlainString();
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
