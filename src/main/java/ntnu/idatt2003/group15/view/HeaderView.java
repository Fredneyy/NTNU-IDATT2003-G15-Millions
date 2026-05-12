package ntnu.idatt2003.group15.view;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class HeaderView {

    private Button settingsBtn;

    public Button getSettingsButton() {
        return settingsBtn;
    }

    public HBox createHeader() {
        HBox header = new HBox();
        header.getStyleClass().add("header-bar");

        // --- Left Side: Logo and Title ---
        HBox logoContainer = new HBox(15);
        logoContainer.setAlignment(Pos.CENTER_LEFT);

        // Gradient Icon Placeholder (The Arrow)
        VBox iconBox = new VBox();
        iconBox.getStyleClass().add("logo-frame");
        Label iconLabel = new Label("↗"); // Replace with GlyphIcon if using FontAwesome
        iconLabel.setStyle("-fx-text-fill: white; -fx-font-size: 20px;");
        iconBox.getChildren().add(iconLabel);

        // Title and Status
        VBox titleBox = new VBox(-2);
        Text stockText = new Text("Stock");
        stockText.getStyleClass().add("brand-text-main");
        Text rushText = new Text("Rush");
        rushText.getStyleClass().addAll("brand-text-main", "brand-text-accent");
        TextFlow brandFlow = new TextFlow(stockText, rushText);

        Text statusText = new Text("Playing as ");
        statusText.getStyleClass().add("status-label");
        Text userName = new Text("asd");
        userName.getStyleClass().addAll("status-label", "status-name");
        TextFlow statusFlow = new TextFlow(statusText, userName);

        titleBox.getChildren().addAll(brandFlow, statusFlow);
        logoContainer.getChildren().addAll(iconBox, titleBox);

        // --- Spacer ---
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // --- Right Side: Actions ---
        HBox actionContainer = new HBox(12);
        actionContainer.setAlignment(Pos.CENTER_RIGHT);

        Button saveBtn = new Button("💾 Save");
        saveBtn.getStyleClass().add("action-button");

        settingsBtn = new Button("⚙");
        settingsBtn.getStyleClass().addAll("action-button", "icon-only-button");

        Label resetLabel = new Label("Reset");
        resetLabel.getStyleClass().add("reset-link");

        // Wrap reset in HBox for padding/alignment
        HBox resetBox = new HBox(resetLabel);
        resetBox.setAlignment(Pos.CENTER);
        resetBox.setPadding(new javafx.geometry.Insets(0, 10, 0, 10));

        Button exitBtn = new Button("↳");
        exitBtn.getStyleClass().addAll("action-button", "icon-only-button");

        actionContainer.getChildren().addAll(saveBtn, settingsBtn, resetBox, exitBtn);

        header.getChildren().addAll(logoContainer, spacer, actionContainer);
        return header;
    }
}