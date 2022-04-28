package mirea.battleship;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import mirea.battleship.Backend.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;

public class App extends Application {
    static final HashMap<String, ShipType[]> shipTypes = new HashMap<>();
    private Stage primaryStage;
    private Battle battle;

    public App() {
        Settings.app = this;
    }

    @Override
    public void start(final Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("Морской бой");
        this.primaryStage.getIcons().add(
                new Image(Objects.requireNonNull(getClass().getResourceAsStream("/mirea/battleship/icon.jpg"))));
        final Scene mainScene = new Scene(new Parent() { });
        mainScene.getStylesheets().add(
                Objects.requireNonNull(getClass().getResource("/mirea/battleship/styles.css")).toExternalForm());
        mainScene.getStylesheets().add(
                Objects.requireNonNull(getClass().getResource(Settings.style.getFilePath())).toExternalForm());
        this.primaryStage.setScene(mainScene);
        this.primaryStage.setMaximized(true);
        this.primaryStage.setResizable(false);
        setScene("MainMenu");
        this.primaryStage.show();
    }

    public void configureBattle() {
        System.out.println("Настройки боя.");
        setScene("ConfigureBattle");
    }

    public void startGame(final BattleSet battleSet) {
        battle = new Battle(battleSet);
        putShips();
    }

    public void putShips() {
        System.out.println(battle.getPlayerName(true) + " расставляет свои корабли.");
        setScene("ArrangeShipsScene");
    }

    public void startBattle() {
        System.out.println("Бой начался!");
        setScene("PlayersBarrier");
    }

    public void finishGame() {
        battle = null;
        setScene("MainMenu");
    }

    public void setScene(final String fileName) {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("layouts/" + fileName + ".fxml"));
        try {
            primaryStage.getScene().setRoot(fxmlLoader.load());
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public Battle getBattle() {
        return battle;
    }

    public static void main(String[] args) {
        shipTypes.put("min", new ShipType[] {
                new ShipType(null, 2, 1),
        });
        shipTypes.put("aLot", new ShipType[] {
                new ShipType("Крейсер", 2, 2),
        });
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
