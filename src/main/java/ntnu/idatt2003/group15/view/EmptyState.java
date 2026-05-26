package ntnu.idatt2003.group15.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.javafx.FontIcon;

public final class EmptyState {

  private EmptyState() {}

  public static VBox create(Ikon icon, String title, String hint) {
    FontIcon iconNode = new FontIcon(icon);
    iconNode.getStyleClass().add("empty-state-icon");
    iconNode.setIconSize(48);
    iconNode.setIconColor(Color.WHITE);
    iconNode.setOpacity(0.55);

    Label titleLabel = new Label(title == null ? "" : title);
    titleLabel.getStyleClass().add("empty-state-title");
    titleLabel.setFont(Font.font(null, FontWeight.BOLD, 16));
    titleLabel.setTextFill(Color.WHITE);

    Label hintLabel = new Label(hint == null ? "" : hint);
    hintLabel.getStyleClass().add("empty-state-hint");
    hintLabel.setWrapText(true);
    hintLabel.setTextAlignment(TextAlignment.CENTER);
    hintLabel.setFont(Font.font(13));
    hintLabel.setTextFill(Color.WHITE);
    hintLabel.setOpacity(0.7);

    VBox box = new VBox(12, iconNode, titleLabel, hintLabel);
    box.setAlignment(Pos.CENTER);
    box.getStyleClass().add("empty-state");
    box.setPadding(new Insets(48, 24, 48, 24));
    box.setMaxWidth(Region.USE_PREF_SIZE);
    return box;
  }
}
