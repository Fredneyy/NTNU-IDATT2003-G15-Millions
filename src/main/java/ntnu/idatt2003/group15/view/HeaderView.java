package ntnu.idatt2003.group15.view;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.kordamp.ikonli.fontawesome.FontAwesome;
import org.kordamp.ikonli.javafx.FontIcon;

public class HeaderView {

    private Button settingsBtn;
    private Text playerNameText;
    private String playerName = "";

    public Button getSettingsButton() {
        return settingsBtn;
    }

    public void setPlayerName(String name) {
        this.playerName = name == null ? "" : name;
        if (playerNameText != null) {
            playerNameText.setText(this.playerName);
        }
    }

    public HBox createHeader() {
        HBox header = new HBox();
        header.getStyleClass().add("header-bar");

        // --- Left Side: Logo and Title ---
        HBox logoContainer = new HBox(15);
        logoContainer.setAlignment(Pos.CENTER_LEFT);

        // Gradient logo tile with a FontAwesome trending-up icon
        StackPane iconBox = new StackPane();
        iconBox.getStyleClass().add("logo-frame");
        FontIcon brandIcon = new FontIcon(FontAwesome.LINE_CHART);
        brandIcon.getStyleClass().add("logo-icon");
        iconBox.getChildren().add(brandIcon);

        // Title and Status
        VBox titleBox = new VBox(-2);
        Text stockText = new Text("Stock");
        stockText.getStyleClass().add("brand-text-main");
        Text rushText = new Text("Rush");
        rushText.getStyleClass().addAll("brand-text-main", "brand-text-accent");
        TextFlow brandFlow = new TextFlow(stockText, rushText);

        Text statusText = new Text("Playing as ");
        statusText.getStyleClass().add("status-label");
        playerNameText = new Text(playerName);
        playerNameText.getStyleClass().addAll("status-label", "status-name");
        TextFlow statusFlow = new TextFlow(statusText, playerNameText);

        titleBox.getChildren().addAll(brandFlow, statusFlow);
        logoContainer.getChildren().addAll(iconBox, titleBox);

        // --- Spacer ---
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // --- Right Side: Actions ---
        HBox actionContainer = new HBox(12);
        actionContainer.setAlignment(Pos.CENTER_RIGHT);

        Button saveBtn = new Button();
        saveBtn.setGraphic(new FontIcon(FontAwesome.FLOPPY_O));
        saveBtn.getStyleClass().add("action-button");

        settingsBtn = new Button();
        settingsBtn.setGraphic(new FontIcon(FontAwesome.COG));
        settingsBtn.getStyleClass().addAll("action-button", "icon-only-button");

        Button resetBtn = new Button();
        resetBtn.setGraphic(new FontIcon(FontAwesome.REFRESH));
        resetBtn.getStyleClass().add("action-button");

        Button exitBtn = new Button();
        exitBtn.setGraphic(new FontIcon(FontAwesome.SIGN_OUT));
        exitBtn.getStyleClass().addAll("action-button", "icon-only-button");

        actionContainer.getChildren().addAll(saveBtn, settingsBtn, resetBtn, exitBtn);

        header.getChildren().addAll(logoContainer, spacer, actionContainer);
        return header;
    }
}