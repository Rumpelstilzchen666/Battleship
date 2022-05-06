package mirea.battleship.Controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.util.converter.IntegerStringConverter;
import mirea.battleship.Backend.BattleSet;
import mirea.battleship.Backend.ShipType;
import mirea.battleship.Settings;

import java.net.URL;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.function.UnaryOperator;

public class ConfigureBattleController implements Initializable {
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
    private ColumnConstraints lenCol;
    @FXML
    private ColumnConstraints nCol;
    @FXML
    private Button addShipTypeButton;
    @FXML
    private Spinner<Integer> gridSizeSpinner;
    @FXML
    private Button doneButton;

    private final ArrayList<ShipType> shipTypes = new ArrayList<>();
    private int gridSize;

    public ConfigureBattleController() {
        //TODO Добавить выбор набора
        shipTypes.addAll(List.of(Settings.DEFAULT_BATTLE_SET.shipTypes()));
        gridSize = Settings.DEFAULT_BATTLE_SET.gridSize();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        gridHBox.setSpacing(Settings.getCellSize());
        GridUI.prepareBattleGrid(gameGrid, gridSize, Settings.getCellSize());
        setShipTypesGrid();

        addShipTypeButton.setMinHeight(Settings.getCellSize());
        addShipTypeButton.setMaxHeight(Settings.getCellSize());
        addShipTypeButton.getStyleClass().addAll("label", "grid", "grid-label");
        addShipTypeButton.setStyle("-fx-font-size: " + Settings.getCellSize() * 0.33 + ';');
        addShipTypeButton.setMaxWidth(Double.MAX_VALUE);

        gridSizeSpinner.setMinWidth(Settings.getCellSize());
        gridSizeSpinner.setPrefWidth(Settings.getCellSize() * 2);
        gridSizeSpinner.setEditable(true);
        gridSizeSpinner.getEditor().setTextFormatter(getIntegerTextFormatter(gridSize));
        gridSizeSpinner.valueProperty().addListener((obs, oldValue, newValue) -> updateGridSize());
        /* Срабатывает при любом изменении текстового поля gridSizeSpinner
        gridSizeSpinner.getEditor().textProperty().addListener((obs, oldValue, newValue) -> {
            if(!"".equals(newValue)) {
                System.out.println("spinner");
            }
        });*/
    }

    //Update TextField value if new text parsed as Integer
    private TextFormatter<Integer> getIntegerTextFormatter(final int defaultValue) {
        final UnaryOperator<TextFormatter.Change> intFilter = c -> {
            if(c.isContentChange()) {
                ParsePosition parsePosition = new ParsePosition(0);
                NumberFormat.getIntegerInstance().parse(c.getControlNewText(), parsePosition);
                if(parsePosition.getIndex() == 0 || parsePosition.getIndex() < c.getControlNewText().length()) {
                    return null;
                }
            }
            return c;
        };
        return new TextFormatter<>(new IntegerStringConverter(), defaultValue, intFilter);
    }

    private void setShipTypesGrid() {
        final int cellSize = Settings.getCellSize(), nRows = shipTypes.size() + 1;
        final double height = cellSize * nRows;
        shipTypesGrid.setMinHeight(height);
        shipTypesGrid.setMaxHeight(height);
        shipTypesGrid.getRowConstraints().addAll(GridUI.getRowConstraintsForGrid(nRows));

        nameCol.setMinWidth(cellSize);
        int maxShipLen = 1;
        for(ShipType shipType : shipTypes) {
            maxShipLen = Math.max(maxShipLen, shipType.len());
        }
        maxShipLen *= cellSize;
        shapeCol.setMinWidth(maxShipLen);
        shapeCol.setMaxWidth(maxShipLen);
        lenCol.setMinWidth(cellSize * 2);
        nCol.setMinWidth(cellSize * 2);

        shipTypesGrid.add(GridUI.getLabelForGrid("Название", cellSize), 0, 0);
        shipTypesGrid.add(GridUI.getLabelForGrid("Длина", cellSize), 2, 0);
        shipTypesGrid.add(GridUI.getLabelForGrid("Количество", cellSize), 3, 0);
        for(int shipTypeN = 0; shipTypeN < shipTypes.size(); shipTypeN++) {
            final int finalShipTypeN = shipTypeN;
            final TextField shipTypeNameTF = new TextField(shipTypes.get(shipTypeN).name());
            shipTypeNameTF.setMaxSize(Double.MAX_VALUE, Settings.getCellSize());
            shipTypeNameTF.getStyleClass().addAll("label", "grid", "grid-label");
            shipTypeNameTF.setStyle("-fx-font-size: " + cellSize * 0.33 + ';');
            shipTypeNameTF.setOnAction(e -> shipTypes.set(finalShipTypeN, new ShipType(shipTypeNameTF.getText(),
                        shipTypes.get(finalShipTypeN).n(), shipTypes.get(finalShipTypeN).len())));
            shipTypesGrid.add(shipTypeNameTF, 0, shipTypeN + 1);

            shipTypesGrid.add(GridUI.getShip(shipTypes.get(shipTypeN).len(), Settings.getCellSize()), 1, shipTypeN + 1);

            final Spinner<Integer> lenShipsSpinner = getSpinner(1, gridSize, shipTypes.get(shipTypeN).len());
            lenShipsSpinner.valueProperty().addListener((obs, oldValue, newValue) -> {
                if(!Objects.equals(oldValue, newValue)) {
                    shipTypes.set(finalShipTypeN,
                            new ShipType(shipTypes.get(finalShipTypeN).name(), shipTypes.get(finalShipTypeN).n(),
                                    newValue));
                    updateShipTypesGrid();
                }
            });
            shipTypesGrid.add(lenShipsSpinner, 2, shipTypeN + 1);

            final Spinner<Integer> nShipsSpinner =
                    getSpinner(-1, getMaxShipsOnGrid(shipTypes.get(shipTypeN).len()), shipTypes.get(shipTypeN).n());
            nShipsSpinner.valueProperty().addListener((obs, oldValue, newValue) -> {
                if(!Objects.equals(oldValue, newValue)) {
                    if(newValue < 0) {
                        shipTypes.remove(finalShipTypeN);
                        updateShipTypesGrid();
                    }
                    else {
                        shipTypes.set(finalShipTypeN, new ShipType(shipTypes.get(finalShipTypeN).name(), newValue,
                                shipTypes.get(finalShipTypeN).len()));
                        doneButton.setDisable(!isBattleSetPlayable());
                    }
                }
            });
            shipTypesGrid.add(nShipsSpinner, 3, shipTypeN + 1);
        }
        doneButton.setDisable(!isBattleSetPlayable());
    }

    private Spinner<Integer> getSpinner(final int min, final int max, final int initialValue) {
        final Spinner<Integer> spinner = new Spinner<>(min, max, initialValue);
        spinner.setMinWidth(Settings.getCellSize());
        spinner.setPrefWidth(Settings.getCellSize() * 2);
        spinner.setMinHeight(Settings.getCellSize() - 2);
        spinner.setMaxHeight(Settings.getCellSize() - 2);
        spinner.setEditable(true);
        spinner.getEditor().setTextFormatter(getIntegerTextFormatter(initialValue));
        return spinner;
    }

    private void updateGridSize() {
        gridSize = gridSizeSpinner.getValue();
        GridUI.cleanGrid(gameGrid);
        GridUI.prepareBattleGrid(gameGrid, gridSize, Settings.getCellSize());
        updateShipTypesGrid();
    }

    private void updateShipTypesGrid() {
        shipTypesGrid.getRowConstraints().clear();
        shipTypesGrid.getChildren().clear();
        //Иначе сетка исчезает
        shipTypesGrid.setGridLinesVisible(false);
        shipTypesGrid.setGridLinesVisible(true);
        setShipTypesGrid();
    }

    private int getMaxShipsOnGrid(final int shipLen) {
        return (getGridWithAureoleArea() / getShipWithAureoleArea(shipLen));
    }

    private boolean isBattleSetPlayable() {
        int sumShipsWithAureoleArea = 0;
        for(ShipType shipType : shipTypes) {
            if(shipType.len() > gridSize) {
                return false;
            }
            sumShipsWithAureoleArea += getShipWithAureoleArea(shipType.len()) * shipType.n();
        }
        return sumShipsWithAureoleArea <= getGridWithAureoleArea();
    }

    private int getShipWithAureoleArea(final int shipLen) {
        return (shipLen + 1) * 2;
    }

    private int getGridWithAureoleArea() {
        return (gridSize + 1) * (gridSize + 1);
    }

    @FXML
    private void addShipType() {
        shipTypes.add(new ShipType(null, 1, 1));
        updateShipTypesGrid();
    }

    @FXML
    private void forward() {
        Settings.getApp().startGame(new BattleSet(gridSize, shipTypes.toArray(new ShipType[0])));
    }

    @FXML
    private void finish() {
        Settings.getApp().finishGame();
    }
}
