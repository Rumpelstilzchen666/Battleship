package mirea.battleship.Controllers;

import mirea.battleship.Settings;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;

import java.net.URL;
import java.util.ResourceBundle;

import static mirea.battleship.Controllers.GridUI.*;

public class MainMenuController implements Initializable {
    @FXML
    private GridPane mainMenuGrid;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setMainMenuGrid();
    }

    private void setMainMenuGrid() {
        final String[] buttonLabels = new String[]{"Новая игра", "Продолжить игру", "Настройки", "Правила", "Выход"};
        final int buttonWidthInCells = 3;
        final int nCols = buttonWidthInCells + 2;
        final int cellSize = 75;
        prepareBattleGrid(mainMenuGrid, buttonLabels.length, nCols, cellSize);

        final double shipWidth = getWidth(getShip(buttonWidthInCells, cellSize)),
                shipHeight = getHeight(getShip(buttonWidthInCells, cellSize));
        for(int rowN = 0; rowN < buttonLabels.length; rowN++) {
            mainMenuGrid.add(getShip(buttonWidthInCells, cellSize),
                    (nCols - buttonWidthInCells) / 2 + 1, rowN + 1, buttonWidthInCells, 1);

            final Button button = new Button('_' + buttonLabels[rowN]);
            button.getStyleClass().add("menu-button");
            button.setStyle("-fx-font-size: " + cellSize / 4 + ';');
            button.setPrefSize(shipWidth, shipHeight);
            switch(rowN) {
                case 0 -> {
                    button.setOnAction(actionEvent -> Settings.getApp().configureBattle());
                    button.setDefaultButton(true);
                }
                case 1 -> button.setOnAction(actionEvent -> Settings.getApp().restartBattle());
                case 4 -> {
                    button.setOnAction(actionEvent -> System.exit(0));
                    button.setCancelButton(true);
                }
                default -> button.setOnAction(null);
            }
            mainMenuGrid.add(button, (nCols - buttonWidthInCells) / 2 + 1, rowN + 1, buttonWidthInCells, 1);
        }
    }
}
