package mirea.battleship.Controllers;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import mirea.battleship.App;
import mirea.battleship.Settings;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

public abstract class PopupMenuController implements Initializable {
    @FXML
    protected Label header;
    @FXML
    protected GridPane popupMenuGrid;

    protected final Stage menuStage;
    protected String[] buttonLabels;
    protected EventHandler<ActionEvent>[] buttonOnActions;

    protected PopupMenuController(Stage menuStage) {
        this.menuStage = menuStage;
    }

    protected static void init(final Stage primaryStage, final PopupMenuController controller) {
        initStage(primaryStage).setScene(initScene(controller));
        setOnEscapePressed(primaryStage, keyEvent -> controller.show());
    }

    protected static void setOnEscapePressed(final Stage stage, final EventHandler<? super KeyEvent> eventHandler) {
        if(stage != null) {
            stage.getScene().setOnKeyPressed(keyEvent -> {
                if(keyEvent.getCode().equals(KeyCode.ESCAPE)) {
                    if(eventHandler != null) {
                        eventHandler.handle(keyEvent);
                    }
                }
            });
        }
    }

    protected static Stage initStage(final Stage primaryStage) {
        final Stage popupMenuStage = new Stage(StageStyle.TRANSPARENT);
        popupMenuStage.initOwner(primaryStage);
        popupMenuStage.initModality(Modality.APPLICATION_MODAL);
        return popupMenuStage;
    }

    protected static Scene initScene(final PopupMenuController controller) {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("layouts/PopupMenu.fxml"));
        fxmlLoader.setController(controller);
        Scene popupMenuScene = null;
        try {
            popupMenuScene = new Scene(fxmlLoader.load(), Color.TRANSPARENT);
        } catch(IOException e) {
            e.printStackTrace();
        }
        Objects.requireNonNull(popupMenuScene).getStylesheets().addAll(
                Objects.requireNonNull(App.class.getResource("/mirea/battleship/styles.css")).toExternalForm(),
                Objects.requireNonNull(App.class.getResource(Settings.style.getFilePath())).toExternalForm());
        return popupMenuScene;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        GridUI.prepareMenuGrid(popupMenuGrid, buttonLabels, buttonOnActions, 0, 0);
        header.setStyle("-fx-font-size: " + Settings.getCellSize() + ';');
    }

    public void show() {
        menuStage.getOwner().getScene().getRoot().setEffect(new GaussianBlur(Settings.getCellSize() * 0.25));
        menuStage.show();
    }

    public void hide() {
        menuStage.getOwner().getScene().getRoot().setEffect(null);
        menuStage.hide();
    }
}
