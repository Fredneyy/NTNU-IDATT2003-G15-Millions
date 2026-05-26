package ntnu.idatt2003.group15.view;

import java.util.Objects;
import javafx.beans.binding.Bindings;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import ntnu.idatt2003.group15.model.stocks.Share;

/**
 * Card sitting under the portfolio table with a single action that liquidates
 * every position the player still holds. The action is disabled when the
 * portfolio is empty so users don't try to cash out nothing.
 */
public class SellAllCard {

  private final VBox view = new VBox();

  public SellAllCard(ObservableList<Share> shares, Runnable onSellAllPressed) {
    Objects.requireNonNull(shares, "shares");
    Objects.requireNonNull(onSellAllPressed, "onSellAllPressed");

    Label title = new Label("Cash Out");
    title.getStyleClass().add("sell-all-title");

    Label subtitle = new Label("Sell every share you currently "
        + "hold and turn your portfolio into cash.");
    subtitle.getStyleClass().add("sell-all-subtitle");
    subtitle.setWrapText(true);

    VBox text = new VBox(4, title, subtitle);
    text.setAlignment(Pos.CENTER_LEFT);

    Button sellAllButton = new Button("Sell All Stocks");
    sellAllButton.getStyleClass().add("sell-all-button");
    sellAllButton.setOnAction(_ -> onSellAllPressed.run());
    sellAllButton.disableProperty().bind(Bindings.isEmpty(shares));

    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);

    HBox row = new HBox(text, spacer, sellAllButton);
    row.setAlignment(Pos.CENTER_LEFT);
    row.getStyleClass().add("sell-all-row");

    view.getChildren().add(row);
    view.getStyleClass().add("sell-all-card");
  }

  public VBox getView() {
    return view;
  }
}
