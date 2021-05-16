package javaCode.Controllers;

import javaCode.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

import java.net.URL;
import java.util.ResourceBundle;

import static javaCode.AppUI.*;

public class ArrangeShipsSceneController implements Initializable {
    @FXML
    private Label header;
    @FXML
    private HBox gridHBox;
    @FXML
    private GridPane gameGrid;
    @FXML
    private GridPane shipTypesGrid;
    @FXML
    private ColumnConstraints nameCol;
    @FXML
    private ColumnConstraints shapeCol;
    @FXML
    private ColumnConstraints nCol;

    private static final Direction DEFAULT_DIRECTION = Direction.RIGHT;
    private static App app;
    private static int playerN;
    private static Grid grid;

    public static void preset(final App app1) {
        if(app1 == null) {
            throw new NullPointerException("App == null");
        }
        app = app1;
        playerN = -1;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if(app == null) {
            throw new IllegalStateException("ArrangeShipsSceneController must be preset");
        }
        playerN = (playerN + 1) % 2;
        grid = app.getBattle().grid[playerN];
        header.setText(app.getBattle().playersNames[playerN] + ", расставьте свои корабли на поле");
        gridHBox.setSpacing(app.getCellSize() * 2);
        prepareBattleGrid(gameGrid, app.getBattle().grid[playerN].getSize(), app.getCellSize());
        setShipTypesGrid();
    }

    private void setShipTypesGrid() {
        final ShipType[] shipTypes = app.getBattle().shipTypes;
        final int cellSize = app.getCellSize();
        final double height = cellSize * shipTypes.length;
        shipTypesGrid.setMinHeight(height);
        shipTypesGrid.setPrefHeight(height);
        shipTypesGrid.setMaxHeight(height);
        shipTypesGrid.getRowConstraints().addAll(getRowConstraintsForGrid(shipTypes.length));

        nameCol.setMinWidth(cellSize);
        int maxShipLen = 0;
        for(ShipType shipType : shipTypes) {
            maxShipLen = Math.max(maxShipLen, shipType.len());
        }
        maxShipLen *= cellSize;
        shapeCol.setMinWidth(maxShipLen);
        shapeCol.setPrefWidth(maxShipLen);
        shapeCol.setMaxWidth(maxShipLen);
        nCol.setMinWidth(cellSize);

        for(int shipTypeN = 0; shipTypeN < shipTypes.length; shipTypeN++) {
            shipTypesGrid.add(getLabelForGrid(shipTypes[shipTypeN].name(), cellSize), 0, shipTypeN);
            final Button button = new Button(null, getShip(shipTypes[shipTypeN].len(), cellSize));
            button.getStyleClass().add("menu-button");
            button.setDisable(true);
            shipTypesGrid.add(button, 1, shipTypeN);
            final Label nShipsLabel = getLabelForGrid(String.valueOf(shipTypes[shipTypeN].n()), cellSize);
            shipTypesGrid.add(nShipsLabel, 2, shipTypeN);
            for(int shipN = 0; shipN < shipTypes[shipTypeN].n(); shipN++) {
                shipTypesGrid.add(new Ship(shipTypes[shipTypeN], shipTypeN, nShipsLabel).display, 1, shipTypeN);
            }
        }
    }

    @FXML
    private void forward() {
        switch(playerN) {
            case 0 -> app.putShips(1);
            case 1 -> app.startBattle();
        }
    }

    @FXML
    private void finish() {
        app.finishGame();
    }

    private class Ship {
        private final ShipType shipType;
        private final int shipTypeRowN;
        private final Label nShipsLabel;
        private final Node display;
        private final double initialTranslateX, initialTranslateY;
        private Direction direction = DEFAULT_DIRECTION;
        private State state = State.PASSIVE;
        private Coordinate sternCoordinate, oldSternCoordinate;
        private boolean located = false;


        public Ship(final ShipType shipType, final int shipTypeRowN, final Label nShipsLabel) {
            if(shipType == null) {
                throw new NullPointerException("shipType == null");
            }
            this.shipType = shipType;
            if(shipTypeRowN < 0) {
                throw new IllegalArgumentException("shipTypeRowN(" + shipTypeRowN + ") < 0");
            }
            this.shipTypeRowN = shipTypeRowN;
            if(nShipsLabel == null) {
                throw new NullPointerException("nShipsLabel == null");
            }
            this.nShipsLabel = nShipsLabel;
            display = getShip(shipType.len(), app.getCellSize());
            initialTranslateX = display.getTranslateX();
            initialTranslateY = display.getTranslateY();
            //TODO Расставлять корабли на поле перетаскиванием
        }

        public boolean locate(final int sternCol, final int sternRow) throws Grid.ShipLocationException {
            removeFromShipTypesGrid();
            return addToGameGrid(sternCol, sternRow);
        }

        private boolean relocate() throws Grid.ShipLocationException {
            return relocate(sternCoordinate.col(), sternCoordinate.row());
        }

        private boolean relocate(final int sternCol, final int sternRow) throws Grid.ShipLocationException {
            removeFromGameGrid();
            return addToGameGrid(sternCol, sternRow);
        }

        public void remove() {
            removeFromGameGrid();
            addToShipTypesGrid();
        }

        private boolean addToGameGrid(final int sternCol, final int sternRow) throws Grid.ShipLocationException {
            if(isLocated()) {
                return relocate(sternCol, sternRow);
            }
            try {
                grid.putProbableShip(sternCol, sternRow, shipType.len(), direction);
            } catch(Grid.ShipLocationException e) {
                setState(State.INCORRECT);
                throw e;
            }
            sternCoordinate = new Coordinate(sternCol, sternRow);
            grid.confirmProbableShip();
            switch(getDirection()) {
                case RIGHT -> gameGrid.add(display, sternCol, sternRow, shipType.len(), 1);
                case DOWN -> gameGrid.add(display, sternCol, sternRow, 1, shipType.len());
                case LEFT -> gameGrid.add(display, sternCol - shipType.len() + 1, sternRow, shipType.len(), 1);
                case UP -> gameGrid.add(display, sternCol, sternRow - shipType.len() + 1, 1, shipType.len());
            }
            setState(State.PASSIVE);
            setLocated(true);
            return true;
        }

        private void removeFromGameGrid() {
            if(isLocated()) {
                try {
                    try {
                        grid.removeShip(sternCoordinate.col(), sternCoordinate.row());
                    } catch(Grid.SelectedCellException e) {
                        throw new RuntimeException("Корабль должен быть установлен в указанных координатах.", e);
                    } finally {
                        oldSternCoordinate = sternCoordinate;
                        gameGrid.getChildren().remove(display);
                        setLocated(false);
                    }
                }
                catch(Grid.RemovalShipException e) {
                    throw new IllegalStateException("Невозможно удалить обстрелянный корабль.", e);
                }
            }
        }

        private void addToShipTypesGrid() {
            try {
                setDirection(DEFAULT_DIRECTION);
            } catch(Grid.ShipLocationException ignored) { }
            shipTypesGrid.add(display, 1, shipTypeRowN);
            nShipsLabel.setText(String.valueOf(Integer.parseInt(nShipsLabel.getText()) + 1));
            setState(State.PASSIVE);
        }

        private void removeFromShipTypesGrid() {
            shipTypesGrid.getChildren().remove(display);
            nShipsLabel.setText(String.valueOf(Integer.parseInt(nShipsLabel.getText()) - 1));
        }

        public Direction getDirection() {
            return direction;
        }

        public void setDirection(final Direction direction) throws Grid.ShipLocationException {
            if(this.direction != direction) {
                rotateDisplay(this.direction, direction);
                this.direction = direction;
                if(isLocated()) {
                    relocate();
                }
            }
        }

        private void rotateDisplay(final Direction oldDirection, final Direction newDirection) {
            if(oldDirection == newDirection) {
                return;
            }
            final double translateX, translateY;
            switch(oldDirection) {
                case RIGHT -> {
                    translateX = display.getTranslateX();
                    translateY = display.getTranslateY();
                }
                case DOWN -> {
                    translateX = display.getTranslateY();
                    translateY = display.getTranslateX();
                }
                case LEFT -> {
                    translateX = - display.getTranslateX();
                    translateY = display.getTranslateY();
                }
                case UP -> {
                    translateX = - display.getTranslateY();
                    translateY = display.getTranslateX();
                }
                default -> throw new IllegalStateException("Unexpected value: " + oldDirection);
            }

            switch(newDirection) {
                case RIGHT -> {
                    display.setRotate(0);
                    display.setTranslateX(translateX);
                    display.setTranslateY(translateY);
                }
                case DOWN -> {
                    display.setRotate(90);
                    display.setTranslateX(translateY);
                    display.setTranslateY(translateX);
                }
                case LEFT -> {
                    display.setRotate(180);
                    display.setTranslateX(- translateX);
                    display.setTranslateY(translateY);
                }
                case UP -> {
                    display.setRotate(270);
                    display.setTranslateX(translateY);
                    display.setTranslateY(- translateX);
                }
            }
        }

        public Node getDisplay() {
            return display;
        }

        private void setLocated(final boolean located) {
            this.located = located;
            if(!this.located) {
                sternCoordinate = null;
            }
        }

        public boolean isLocated() {
            return located;
        }

        public void setState(State state) {
            if(this.state != state) {
                if(state == State.PASSIVE) {
                    if(this.state == State.INCORRECT) {
                        if(oldSternCoordinate != null) {
                            try {
                                relocate(oldSternCoordinate.col(), oldSternCoordinate.row());
                            } catch(Grid.ShipLocationException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                this.state = state;
                display.getStyleClass().add(switch(this.state) {
                    case PASSIVE -> "ok-ship";
                    case ACTIVE -> "probable-ship";
                    case INCORRECT -> "incorrect-ship";
                });

            }
        }

        public State getState() {
            return state;
        }

        private enum State {
            PASSIVE, ACTIVE, INCORRECT
        }
    }
}
