package ntnu.idatt2003.group15.view;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.kordamp.ikonli.fontawesome.FontAwesome;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.materialdesign2.MaterialDesignC;
import org.kordamp.ikonli.materialdesign2.MaterialDesignL;

import java.util.Objects;

public class HeaderView {

    private Button settingsBtn;
    private Button saveBtn;
    private Button advanceWeekBtn;
    private CheckBox autoAdvanceCheckBox;
    private Text playerNameText;
    private String playerName = "";
    private final Runnable exit;

    public HeaderView(Runnable runnableExit) {
        this.exit = Objects.requireNonNull(runnableExit);
    }

    public Button getSettingsButton() {
        return settingsBtn;
    }

    public Button getSaveButton() {
        return saveBtn;
    }

    public Button getAdvanceWeekButton() {
        return advanceWeekBtn;
    }

    public CheckBox getAutoAdvanceCheckBox() {
        return autoAdvanceCheckBox;
    }

    public void setPlayerName(String name) {
        this.playerName = name == null ? "" : name;
        if (playerNameText != null) {
            playerNameText.setText(this.playerName);
        }
    }

    public HBox createHeader() {
        HBox header = new HBox();
        header.getStylesheets().add(
            Objects.requireNonNull(getClass().getResource("/style/HeaderStyle.css")).toExternalForm());
        header.getStyleClass().add("header-bar");

        HBox logoContainer = new HBox(15);
        logoContainer.setAlignment(Pos.CENTER_LEFT);

        StackPane iconBox = new StackPane();
        iconBox.getStyleClass().add("logo-frame");
        FontIcon brandIcon = new FontIcon(FontAwesome.LINE_CHART);
        brandIcon.getStyleClass().add("logo-icon");
        iconBox.getChildren().add(brandIcon);

        VBox titleBox = new VBox(-2);
        Text stockText = new Text("Millions");
        stockText.getStyleClass().add("brand-text-main");
        TextFlow brandFlow = new TextFlow(stockText);

        Text statusText = new Text("Playing as ");
        statusText.getStyleClass().add("status-label");
        playerNameText = new Text(playerName);
        playerNameText.getStyleClass().addAll("status-label", "status-name");
        TextFlow statusFlow = new TextFlow(statusText, playerNameText);

        titleBox.getChildren().addAll(brandFlow, statusFlow);
        logoContainer.getChildren().addAll(iconBox, titleBox);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox actionContainer = new HBox(12);
        actionContainer.setAlignment(Pos.CENTER_RIGHT);

        advanceWeekBtn = new Button("Advance Week");
        advanceWeekBtn.setGraphic(new FontIcon(MaterialDesignC.CALENDAR_ARROW_RIGHT));
        advanceWeekBtn.getStyleClass().addAll("action-button", "action-button-primary");

        autoAdvanceCheckBox = new CheckBox("Auto-advance");
        autoAdvanceCheckBox.getStyleClass().add("auto-advance-toggle");
        autoAdvanceCheckBox.setSelected(true);

        saveBtn = new Button();
        saveBtn.setGraphic(new FontIcon(MaterialDesignC.CONTENT_SAVE_OUTLINE));
        saveBtn.getStyleClass().addAll("action-button", "icon-only-button");

        settingsBtn = new Button();
        settingsBtn.setGraphic(new FontIcon(MaterialDesignC.COG_OUTLINE));
        settingsBtn.getStyleClass().addAll("action-button", "icon-only-button");

        Button exitBtn = new Button();
        exitBtn.setGraphic(new FontIcon(MaterialDesignL.LOGOUT));
        exitBtn.getStyleClass().addAll("action-button", "icon-only-button", "action-button-danger");
        exitBtn.setOnAction(_ -> exit.run());
        actionContainer.getChildren().addAll(
            autoAdvanceCheckBox, advanceWeekBtn, saveBtn, settingsBtn, exitBtn);

        header.getChildren().addAll(logoContainer, spacer, actionContainer);
        return header;
    }
}