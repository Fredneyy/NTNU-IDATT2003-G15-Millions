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

/**
 * Shared empty-state panel used across the game tabs (News, Orders,
 * Portfolio, Stats). Renders a muted icon, a title, and a hint, centered with
 * consistent typography so every tab tells the user the same way that there's
 * nothing here yet. Style classes are exposed on each node so a stylesheet
 * can theme the look without touching Java; the inline styles act as a
 * sensible default if no CSS rules are present.
 */
public final class EmptyState {

  private EmptyState() {}

  /**
   * Build an empty-state panel with the given icon, title, and hint text.
   *
   * @param icon  icon shown above the title
   * @param title short headline (e.g. "No trades yet")
   * @param hint  one or two lines of guidance — may contain newlines
   * @return a centered {@link VBox} suitable for direct insertion into a
   *         layout slot, a {@code StackPane} overlay, or a
   *         {@code TableView.setPlaceholder(...)} call
   */
  public static VBox create(Ikon icon, String title, String hint) {
    // programmatic setters only, setStyle() on FontIcon clobbers -fx-icon-code on some ikonli versions
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
    // generous vertical padding so the panel breathes in both placeholder and overlay slots
    box.setPadding(new Insets(48, 24, 48, 24));
    box.setMaxWidth(Region.USE_PREF_SIZE);
    return box;
  }
}
