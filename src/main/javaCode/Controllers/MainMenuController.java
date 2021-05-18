package javaCode.Controllers;

import javaCode.App;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;

import java.net.URL;
import java.util.ResourceBundle;

import static javaCode.AppUI.*;

public class MainMenuController implements Initializable {
    private static App app;
    @FXML
    private GridPane mainMenuGrid;

    public static void preset(final App app) {
        if(app == null) {
            throw new NullPointerException("App == null");
        }
        MainMenuController.app = app;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if(app == null) {
            throw new IllegalStateException("MainMenuController must be preset");
        }
        setMainMenuGrid();
    }

    private void setMainMenuGrid() {
        final String[] buttonLabels = new String[]{"Играть", "Настройки", "Правила", "Выход"};
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
                case 0 -> button.setOnAction(actionEvent -> app.startGame());
                case 3 -> button.setOnAction(actionEvent -> System.exit(0));
                default -> button.setOnAction(null);
            }
            mainMenuGrid.add(button, (nCols - buttonWidthInCells) / 2 + 1, rowN + 1, buttonWidthInCells, 1);
        }
    }
}
