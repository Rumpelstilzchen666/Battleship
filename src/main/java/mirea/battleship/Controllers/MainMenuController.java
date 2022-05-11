package mirea.battleship.Controllers;

import mirea.battleship.Settings;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.GridPane;

import java.net.URL;
import java.util.ResourceBundle;

public class MainMenuController implements Initializable {
    @FXML
    private GridPane mainMenuGrid;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        GridUI.prepareMenuGrid(mainMenuGrid,
                new String[]{"Новая игра", "Продолжить игру", "Настройки", "Правила", "Выход"},
                new EventHandler[]{
                        actionEvent -> Settings.getApp().configureBattle(),
                        actionEvent -> Settings.getApp().restartBattle(),
                        null,
                        null,
                        actionEvent -> System.exit(0)},
                0, 4);
    }
}
