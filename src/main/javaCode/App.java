package javaCode;

import javaCode.Controllers.ArrangeShipsSceneController;
import javaCode.Controllers.MainMenuController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;

public class App extends Application {
    private static final HashMap<String, ShipType[]> shipTypes = new HashMap<>();
    private int playerN;
    private Stage primaryStage;
    private Battle battle;
    private final int cellSize = 50;

    public App() { }

    @Override
    public void start(final Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("Морской бой");
        this.primaryStage.getIcons().add(
                new Image(Objects.requireNonNull(getClass().getResourceAsStream("/resources/icon.jpg"))));
        final Scene mainScene = new Scene(new Parent() { });
        mainScene.getStylesheets().add(
                Objects.requireNonNull(getClass().getResource("/resources/styles.css")).toExternalForm());
        mainScene.getStylesheets().add(
                Objects.requireNonNull(getClass().getResource(Settings.style.getFilePath())).toExternalForm());
        this.primaryStage.setScene(mainScene);
        this.primaryStage.setMaximized(true);
        this.primaryStage.setResizable(false);
        MainMenuController.preset(this);
        setMainMenuScene();
        this.primaryStage.show();
    }

    private void setMainMenuScene() {
        try {
            primaryStage.getScene().setRoot(
                    new FXMLLoader(getClass().getResource("/resources/layouts/MainMenu.fxml")).load());
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public void startGame() {
        battle = new Battle(10, shipTypes.get("ruWiki"));
        ArrangeShipsSceneController.preset(this);
        putShips(0);
    }

    public void putShips(final int playerN) {
        this.playerN = playerN;
        System.out.println(battle.playersNames[playerN] + " расставляет свои корабли.");
        try {
            primaryStage.getScene().setRoot(
                    new FXMLLoader(getClass().getResource("/resources/layouts/arrangeShipsScene.fxml")).load());
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public void startBattle() {
        System.out.println("Бой начался!");
    }

    public void finishGame() {
        battle = null;
        setMainMenuScene();
    }

    public int getPlayerN() {
        return playerN;
    }

    public Battle getBattle() {
        return battle;
    }

    public int getCellSize() {
        return cellSize;
    }

    public static void main(String[] args) {
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
        Application.launch();
    }
}
