package mirea.battleship.Controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Shape;
import mirea.battleship.Backend.*;
import mirea.battleship.Settings;
import mirea.battleship.XMLTools;

import java.net.URL;
import java.util.*;

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
    @FXML
    private Button doneButton;

    private static final Direction DEFAULT_DIRECTION = Direction.RIGHT;
    private final Battle battle;
    private final Grid grid;
    private final ShipType[] shipTypes;
    private final ArrayList<Ship> ships = new ArrayList<>();
    private final Label[] nShipsLabels;

    public ArrangeShipsSceneController() {
        battle = Settings.getApp().getBattle();
        grid = battle.getGrid(true);
        shipTypes = battle.getShipTypes();
        nShipsLabels = new Label[shipTypes.length];
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        PopupBattleMenuController.init(Settings.getApp().getPrimaryStage(), actionEvent -> XMLTools.saveBattle(battle));
        header.setText(battle.getPlayerName(true) + ", расставьте свои корабли на поле");
        gridHBox.setSpacing(Settings.getCellSize());
        GridUI.prepareBattleGrid(gameGrid, grid.getSize(), Settings.getCellSize());
        setShipTypesGrid();
    }

    private void setShipTypesGrid() {
        final int cellSize = Settings.getCellSize();
        final double height = cellSize * shipTypes.length;
        shipTypesGrid.setMinHeight(height);
        shipTypesGrid.setMaxHeight(height);
        shipTypesGrid.getRowConstraints().addAll(GridUI.getRowConstraintsForGrid(shipTypes.length));

        nameCol.setMinWidth(cellSize);
        int maxShipLen = 0;
        for(ShipType shipType : shipTypes) {
            maxShipLen = Math.max(maxShipLen, shipType.len());
        }
        maxShipLen *= cellSize;
        shapeCol.setMinWidth(maxShipLen);
        shapeCol.setMaxWidth(maxShipLen);
        nCol.setMinWidth(cellSize);

        for(int shipTypeN = 0; shipTypeN < shipTypes.length; shipTypeN++) {
            shipTypesGrid.add(GridUI.getLabelForGrid(shipTypes[shipTypeN].name(), cellSize), 0, shipTypeN);
            final Button button = new Button(null, GridUI.getShip(shipTypes[shipTypeN].len(), cellSize));
            button.getStyleClass().add("menu-button");
            button.setDisable(true);
            shipTypesGrid.add(button, 1, shipTypeN);
            nShipsLabels[shipTypeN] = GridUI.getLabelForGrid("0", cellSize);
            shipTypesGrid.add(nShipsLabels[shipTypeN], 2, shipTypeN);

            for(int shipN = 0; shipN < shipTypes[shipTypeN].n(); shipN++) {
                ships.add(new Ship(shipTypeN));
                ships.get(ships.size() - 1).addToShipTypesGrid();
            }
        }
        ships.trimToSize();
    }

    @FXML
    private void forward() {
        battle.nextPlayer();
        switch(battle.getPlayerN(false)) {
            case 0 -> Settings.getApp().putShips();
            case 1 -> Settings.getApp().startBattle();
        }
    }

    private void updateDoneButtonDisable() {
        for(int shipTypeN = 0; shipTypeN < shipTypes.length; shipTypeN++) {
            if(Integer.parseInt(nShipsLabels[shipTypeN].getText()) != 0) {
                setDoneButtonDisable(true);
                return;
            }
        }
        setDoneButtonDisable(false);
    }

    private void setDoneButtonDisable(final boolean disable) {
        doneButton.setDisable(disable);
    }

    private class Ship {
        private final int shipTypeN;
        private final Node display;
        private final DragContext dragContext = new DragContext();
        private Direction direction = DEFAULT_DIRECTION, prevDirection;
        private State state;
        private Coordinate sternCoordinate, prevSternCoordinate;
        private Location location;
        // Если корабль, ранее установленный на поле, выходит за границы сетки, то размеры сетки изменяются с учётом
        // выступающих частей корабля, что влияет на расчётные размеры ячейки -> координаты корабля
        private Bounds gameGridBounds;

        public Ship(final int shipTypeN) {
            if(shipTypeN < 0) {
                throw new IllegalArgumentException("shipTypeN(" + shipTypeN + ") < 0");
            }
            this.shipTypeN = shipTypeN;
            display = GridUI.getShip(getLength(), Settings.getCellSize());
            display.setOnMousePressed(mouseEvent -> {
                dragContext.set(display.getTranslateX() - mouseEvent.getSceneX(),
                        display.getTranslateY() - mouseEvent.getSceneY());
                gameGridBounds = gameGrid.localToScene(gameGrid.getBoundsInLocal());
                toFront();
                removeFromGrid();
                setState(State.CORRECT);
                mouseEvent.consume();
            });

            display.setOnMouseDragged(mouseEvent -> {
                display.setTranslateX(dragContext.x + mouseEvent.getSceneX());
                display.setTranslateY(dragContext.y + mouseEvent.getSceneY());
                updateState(getCurrentCoordinate());
                mouseEvent.consume();
            });

            final List<Direction> directionOrder =
                    Arrays.asList(Direction.RIGHT, Direction.DOWN, Direction.LEFT, Direction.UP);
            display.setOnScroll(scrollEvent -> {
                final int directionN = directionOrder.indexOf(getDirection()),
                        newDirectionN = (directionN + (scrollEvent.getDeltaY() < 0 ? 1 : (directionOrder.size() - 1))) %
                                directionOrder.size();
                setDirection(directionOrder.get(newDirectionN));
                scrollEvent.consume();
            });

            display.setOnMouseReleased(mouseEvent -> {
                if(!getState().isActive()) {
                    throw new IllegalStateException();
                }
                relocate(getCurrentCoordinate());
                setInitialTranslate();
                mouseEvent.consume();
            });
        }

        private Coordinate getCurrentCoordinate() {
            final Bounds displayBounds = display.localToScene(display.getBoundsInLocal());
            final int gridSize = grid.getSize();
            final double cellSize = gameGridBounds.getWidth() / (gridSize + 1),
                    displayMinSize = Math.min(displayBounds.getHeight(), displayBounds.getWidth()),
                    gameGridLeft = gameGridBounds.getMinX() + cellSize,
                    gameGridTop  = gameGridBounds.getMinY() + cellSize,
                    displaySternCenterX, displaySternCenterY;
            switch(getDirection()) {
                case RIGHT, DOWN -> {
                    displaySternCenterX = displayBounds.getMinX() + displayMinSize / 2;
                    displaySternCenterY = displayBounds.getMinY() + displayMinSize / 2;
                }
                case LEFT, UP -> {
                    displaySternCenterX = displayBounds.getMaxX() - displayMinSize / 2;
                    displaySternCenterY = displayBounds.getMaxY() - displayMinSize / 2;
                }
                default -> throw new IllegalStateException("Unexpected value: " + getDirection());
            }
            final int sternColN = (int) Math.floor((displaySternCenterX - gameGridLeft) / cellSize),
                    sternRowN = (int) Math.floor((displaySternCenterY - gameGridTop) / cellSize);
            return (sternColN >= 0 && sternColN < gridSize && sternRowN >= 0 && sternRowN < gridSize) ?
                    new Coordinate(sternColN, sternRowN) : null;
        }

        private void updateState(final Coordinate coordinate) {
            setState(isOkAddToGameGrid(coordinate) ? State.CORRECT : State.INCORRECT);
        }

        private void setInitialTranslate() {
            display.setTranslateX(0);
            display.setTranslateY(0);
        }

        /**
         * Перемещает {@code display} на передний план относительно других кораблей.
         */
        private void toFront() {
            switch(location) {
                case SHIP_TYPES_GRID -> {
                    shipTypesGrid.getChildren().remove(display);
                    shipTypesGrid.add(display, 1, shipTypeN);
                }
                case GRID, GAME_GRID -> {
                    gameGrid.getChildren().remove(display);
                    GridUI.addShipToGrid(gameGrid, display, sternCoordinate, getLength(), getDirection());
                }
            }
        }

        private boolean relocate(final Coordinate coordinate) {
            removeFromGameGrid();
            removeFromShipTypesGrid();
            if(coordinate == null) {
                return addToShipTypesGrid();
            } else {
                try {
                    return addToGridAndGameGrid(coordinate);
                } catch(Grid.ShipLocationException e) {
                    return setToPrevLocation();
                }
            }
        }

        private boolean isOkAddToGameGrid(final Coordinate coordinate) {
            if(coordinate == null) {
                return false;
            }
            try {
                grid.isOkPutShip(coordinate.col(), coordinate.row(), getLength(), getDirection());
            } catch(Grid.ShipLocationException e) {
                return false;
            }
            return true;
        }

        private boolean addToGridAndGameGrid(final Coordinate coordinate) throws Grid.ShipLocationException {
            if(coordinate == null || getLocation() == Location.GAME_GRID) {
                return false;
            }
            battle.addShip(shipTypeN, coordinate, getDirection());
            sternCoordinate = coordinate;
            GridUI.addShipToGrid(gameGrid, display, sternCoordinate, getLength(), getDirection());
            setLocation(Location.GRID);
            setState(State.PASSIVE);
            updateDoneButtonDisable();
            prevDirection = null;
            prevSternCoordinate = null;
            return true;
        }

        private void removeFromGrid() {
            if(getLocation() != Location.GRID) {
                return;
            }
            try {
                setLocation(Location.GAME_GRID);
                try {
                    if(prevDirection == null) {
                        prevDirection = getDirection();
                    }
                    prevSternCoordinate = sternCoordinate;
                    setDoneButtonDisable(true);
                    battle.removeShip(sternCoordinate);
                } catch(Grid.SelectedCellException e) {
                    removeFromGameGrid();
                    throw new RuntimeException("Корабль должен быть установлен в указанных координатах.", e);
                } finally {
                    sternCoordinate = null;
                }
            } catch(Grid.RemovalShipException e) {
                throw new IllegalStateException("Невозможно удалить обстрелянный корабль.", e);
            }
        }

        private void removeFromGameGrid() {
            if(getLocation() == null || !getLocation().onGrid()) {
                return;
            }
            if(getLocation() == Location.GRID) {
                removeFromGrid();
            }
            gameGrid.getChildren().remove(display);
            setLocation(null);
        }

        private boolean addToShipTypesGrid() {
            if(getLocation() == Location.SHIP_TYPES_GRID) {
                return false;
            }
            setDirection(DEFAULT_DIRECTION);
            shipTypesGrid.add(display, 1, shipTypeN);
            nShipsLabels[shipTypeN].setText(String.valueOf(Integer.parseInt(nShipsLabels[shipTypeN].getText()) + 1));
            setLocation(Location.SHIP_TYPES_GRID);
            setState(State.PASSIVE);
            prevDirection = null;
            prevSternCoordinate = sternCoordinate = null;
            return true;
        }

        private void removeFromShipTypesGrid() {
            if(getLocation() != Location.SHIP_TYPES_GRID) {
                return;
            }
            shipTypesGrid.getChildren().remove(display);
            nShipsLabels[shipTypeN].setText(String.valueOf(Integer.parseInt(nShipsLabels[shipTypeN].getText()) - 1));
            setLocation(null);
        }

        private boolean setToPrevLocation() {
            if(prevSternCoordinate == null) {
                return addToShipTypesGrid();
            }
            setDirection(prevDirection != null ? prevDirection : DEFAULT_DIRECTION);
            try {
                return addToGridAndGameGrid(prevSternCoordinate);
            } catch(Grid.ShipLocationException shipLocationException) {
                throw new IllegalStateException("oldSternCoordinate must be correct", shipLocationException);
            }
        }

        public Coordinate getSternCoordinate() {
            return sternCoordinate;
        }

        private int getLength() {
            return shipTypes[shipTypeN].len();
        }

        public Direction getDirection() {
            return direction;
        }

        private void setDirection(final Direction direction) {
            if(this.direction != direction && (getLocation() != Location.SHIP_TYPES_GRID || getState().isActive())) {
                final Direction prevDirection = this.direction;
                this.direction = direction;
                GridUI.rotateShip(display, this.direction);
                if(!getState().isActive() && getLocation() == Location.GRID) {
                    this.prevDirection = prevDirection;
                    relocate(sternCoordinate);
                }
            }
        }

        private void setLocation(final Location location) {
            this.location = location;
            if(this.location == Location.SHIP_TYPES_GRID) {
                sternCoordinate = null;
            }
        }

        private Location getLocation() {
            return location;
        }

        private void setState(State state) {
            if(this.state != state) {
                this.state = state;
                ((Shape) display).setFill(switch(this.state) {
                    case PASSIVE, CORRECT -> new Color(144F / 255, 152F / 255, 160F / 255, 1F);
                    case INCORRECT -> Color.FIREBRICK;
                });
                display.setOpacity(switch(this.state) {
                    case PASSIVE -> 1;
                    case CORRECT, INCORRECT -> 0.7;
                });

            }
        }

        private State getState() {
            return state;
        }

        private class DragContext {
            double x;
            double y;

            public void set(final double x, final double y) {
                this.x = x;
                this.y = y;
            }
        }

        private enum State {
            PASSIVE, CORRECT, INCORRECT;

            public boolean isActive() {
                return this == CORRECT || this == INCORRECT;
            }
        }

        private enum Location {
            SHIP_TYPES_GRID, GRID, GAME_GRID;

            public boolean onGrid() {
                return this == GRID || this == GAME_GRID;
            }
        }
    }
}
