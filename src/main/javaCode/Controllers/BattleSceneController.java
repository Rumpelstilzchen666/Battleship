package javaCode.Controllers;

import javaCode.Battle;
import javaCode.Coordinate;
import javaCode.Grid;
import javaCode.Settings;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import static javaCode.GridUI.*;

public class BattleSceneController implements Initializable {
    @FXML
    private Label header;
    @FXML
    private GridPane mineGameGrid;
    @FXML
    private GridPane enemyGameGrid;
    @FXML
    private Label resultLabel;
    @FXML
    private Button doneButton;

    private final Battle battle;
    private boolean fired;

    public BattleSceneController() {
        battle = Settings.getApp().getBattle();
        fired = false;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        header.setText(Settings.getApp().getBattle().getPlayerName(true) + ", ваш ход");
        setGameGrid(mineGameGrid, true);
        setGameGrid(enemyGameGrid, false);
    }

    private void setGameGrid(GridPane gameGrid, final boolean mine) {
        final int gridSize = battle.getGrid(mine).getSize();
        final int cellSize = Settings.getCellSize();
        prepareBattleGrid(gameGrid, gridSize, cellSize);
        final ArrayList<Battle.Ship> ships  = battle.getShips(mine);
        final Grid.CellState[][] grid = battle.getGrid(mine).getGrid();
        for(Battle.Ship ship : ships) {
            if(mine || grid[ship.sternCoordinate().row()][ship.sternCoordinate().col()] == Grid.CellState.SUNK) {
                addShipToGrid(gameGrid, Settings.getCellSize(), ship.sternCoordinate(), ship.shipType().len(),
                        ship.direction());
            }
        }
        for(int row = 0; row < gridSize; row++) {
            for(int col = 0; col < gridSize; col++) {
                switch(grid[row][col]) {
                    case MISS, AUREOLE -> gameGrid.add(getFireMark(cellSize, false), col + 1, row + 1);
                    case HIT, SUNK -> gameGrid.add(getFireMark(cellSize, true), col + 1, row + 1);
                }
                if(!mine) {
                    final Button cellButton = new Button();
                    cellButton.getStyleClass().add("cell-button");
                    cellButton.setMinSize(cellSize, cellSize);
                    cellButton.setMaxSize(cellSize, cellSize);
                    final int finalColN = col, finalRowN = row;
                    cellButton.setOnAction(actionEvent -> fire(new Coordinate(finalColN, finalRowN)));
                    gameGrid.add(cellButton, col + 1, row + 1);
                }
            }
        }
    }

    private void fire(final Coordinate coordinate) {
        System.out.println(coordinate);
        if(fired) {
            return;
        }
        final Grid.FireResult fireResult;
        try {
            fireResult = battle.fire(coordinate);
        } catch(Grid.SelectedCellException e) {
            setResultLabel(e.getMessage(), true);
            return;
        }
        if(fireResult == Grid.FireResult.MISS || fireResult == Grid.FireResult.HIT) {
            setResultLabel(fireResult == Grid.FireResult.MISS ? "Мимо" : "Ранен", false);
            enemyGameGrid.add(getFireMark(Settings.getCellSize(), fireResult == Grid.FireResult.HIT),
                    coordinate.col() + 1, coordinate.row() + 1);
        }
        if(fireResult == Grid.FireResult.SUNK) {
            enemyGameGrid.getColumnConstraints().clear();
            enemyGameGrid.getRowConstraints().clear();
            enemyGameGrid.getChildren().clear();
            setGameGrid(enemyGameGrid, false);
            final Battle.Ship ship = battle.getShip(coordinate);
            setResultLabel(ship.shipType().name() + " противника потоплен!", false);
        }

        if(fireResult == Grid.FireResult.MISS) {
            fired = true;
            doneButton.setDisable(false);
        }
        battle.getGrid(false).printGrid(true);
    }

    private void setResultLabel(final String text, final boolean error) {
        resultLabel.setText(text);
        ObservableList<String> styleClasses = resultLabel.getStyleClass();
        if(error && !styleClasses.get(styleClasses.size() - 1).equals("error-label")) {
            styleClasses.add("error-label");
        }
        else if(!error && styleClasses.get(styleClasses.size() - 1).equals("error-label")) {
            styleClasses.remove(styleClasses.size() - 1);
        }
    }

    @FXML
    private void forward() {
        battle.nextPlayer();
        Settings.getApp().setScene("BattleScene");
    }

    @FXML
    private void finish() {
        Settings.getApp().finishGame();
    }
}
