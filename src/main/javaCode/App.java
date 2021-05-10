package javaCode;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Objects;

import static javaCode.AppUI.*;

public class App extends Application {
    private static final HashMap<String, ShipType[]> shipTypes = new HashMap<>();
    private Stage primaryStage;
    private Battle battle;

    public App() {
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("Морской бой");
        this.primaryStage.getIcons().add(
                new Image(Objects.requireNonNull(getClass().getResourceAsStream("/resources/icon.jpg"))));
        this.primaryStage.setScene(getMainMenuScene());
        //this.primaryStage.setFullScreen(true);
        this.primaryStage.show();
    }

    private Scene getMainMenuScene() {
        final Label mainLabel = new Label("Морской бой");
        mainLabel.setId("main-label");
        final GridPane mainMenuGrid = getMineMenuGrid();
        mainMenuGrid.setAlignment(Pos.TOP_CENTER);
        final VBox vBox = new VBox(mainLabel, mainMenuGrid);
        vBox.setAlignment(Pos.TOP_CENTER);
        vBox.setId("main-menu");
        final Scene mainMenuScene = new Scene(vBox);
        mainMenuScene.getStylesheets().add(
                Objects.requireNonNull(getClass().getResource("/resources/styles.css")).toExternalForm());
        return mainMenuScene;
    }

    private GridPane getMineMenuGrid() {
        final String[] buttonLabels = new String[] {"Играть", "Настройки", "Правила", "Выход"};
        final int buttonWidthInCells = 3;
        final int nCols = buttonWidthInCells + 2;
        final int cellSize = 100;
        final GridPane grid = getBattleGrid(buttonLabels.length, nCols, cellSize);

        final double shipWidth = getWidth(getShipPolygon(buttonWidthInCells, cellSize)),
                shipHeight = getHeight(getShipPolygon(buttonWidthInCells, cellSize));
        for(int rowN = 0; rowN < buttonLabels.length; rowN++) {
            grid.add(getShipPolygon(buttonWidthInCells, cellSize),
                    (nCols - buttonWidthInCells) / 2 + 1, rowN + 1, buttonWidthInCells, 1);

            Button button = new Button(buttonLabels[rowN]);
            button.getStyleClass().add("main-menu-button");
            button.setStyle("-fx-font-size: " + cellSize / 4 + ';');
            button.setPrefSize(shipWidth, shipHeight);
            switch(rowN) {
                case 0 -> button.setOnAction(actionEvent -> startBattle());
                case 3 -> button.setOnAction(actionEvent -> System.exit(0));
                default -> button.setOnAction(null);
            }
            grid.add(button, (nCols - buttonWidthInCells) / 2 + 1, rowN + 1, buttonWidthInCells, 1);
        }
        return grid;
    }

    private void startBattle() {
        battle = new Battle(10, shipTypes.get("ruWiki"));
        System.out.println("Игра началась!");
    }

    public static void main(String[] args) {
        Application.launch();
        shipTypes.put("ruWiki", new ShipType[] {
                new ShipType("Линкор", 1, 4),
                new ShipType("Крейсер", 2, 3),
                new ShipType("Эсминец", 3, 2),
                new ShipType("Торпедный катер", 4, 1)
        });
        shipTypes.put("Hasbro", new ShipType[] {
                new ShipType("Авианосец", 1, 5),
                new ShipType("Линкор", 1, 4),
                new ShipType("Эсминец", 1, 3),
                new ShipType("Подлодка", 1, 3),
                new ShipType("Сторожевой корабль", 1, 2)
        });
        shipTypes.put("mine", new ShipType[] {
                new ShipType("Авианосец", 1, 4),
                new ShipType("Крейсер", 1, 3),
                new ShipType("Подлодка", 1, 3),
                new ShipType("Ракетный катер", 4, 2)
        });
    }
}
