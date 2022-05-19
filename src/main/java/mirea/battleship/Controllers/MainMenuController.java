package mirea.battleship.Controllers;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.GridPane;
import mirea.battleship.Settings;

import java.net.URL;
import java.util.ResourceBundle;

public class MainMenuController implements Initializable {
    @FXML
    private GridPane mainMenuGrid;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        PopupMenuController.setOnEscapePressed(Settings.getApp().getPrimaryStage(), null);
        final PopupMenuController popupMenu = PopupOptionMenuController.init(Settings.getApp().getPrimaryStage(),
                "Вы действительно хотите выйти из игры?", new EventHandler[]{event -> System.exit(0), null});
        GridUI.prepareMenuGrid(mainMenuGrid,
                new String[]{"Новая игра", "Продолжить игру", "Настройки", "Правила", "Выход"},
                new EventHandler[]{
                        actionEvent -> Settings.getApp().configureBattle(),
                        actionEvent -> Settings.getApp().restartBattle(),
                        null,
                        null,
                        actionEvent -> popupMenu.show()},
                0, 4);
    }
}
