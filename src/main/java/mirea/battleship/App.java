package mirea.battleship;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import mirea.battleship.Backend.Battle;
import mirea.battleship.Backend.BattleSet;
import mirea.battleship.Controllers.ConfigureBattleController;
import mirea.battleship.Controllers.PopupBattleMenuController;

import java.io.IOException;
import java.util.Objects;

public class App extends Application {
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
        mainScene.getStylesheets().addAll(
                Objects.requireNonNull(App.class.getResource("/mirea/battleship/styles.css")).toExternalForm(),
                Objects.requireNonNull(App.class.getResource(Settings.style.getFilePath())).toExternalForm());
        this.primaryStage.setScene(mainScene);
        this.primaryStage.setMaximized(true);
        this.primaryStage.setResizable(false);
        setScene("MainMenu");
        this.primaryStage.show();
    }

    public void configureBattle() {
        System.out.println("Настройки боя.");
        BattleSet battleSet;
        try {
            battleSet = XMLTools.getBattleSet();
        } catch(IOException e) {
            e.printStackTrace();
            battleSet = new BattleSet(Settings.DEFAULT_SIZE, null);
        }
        setScene("ConfigureBattle", new ConfigureBattleController(battleSet));
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
        // По-хорошему это не сюда, но иначе придётся пересоздавать на каждой смене хода
        // (в конструкторе PlayersBarrierController) или держать там флаг, что меню уже создано и,
        // что вообще непонятно как, менять его при окончании боя и возврате в главное меню.
        PopupBattleMenuController.init(primaryStage, actionEvent -> XMLTools.saveBattle(Settings.getApp().getBattle()));
        setScene("PlayersBarrier");
    }

    public void finishGame() {
        battle = null;
        setScene("MainMenu");
    }

    public void restartBattle() {
        try {
            battle = XMLTools.getBattle();
        } catch(IllegalXMLException | IOException e) {
            e.printStackTrace();
        }
        if(battle != null) {
            if(battle.allShipsArranged(true) && battle.allShipsArranged(false)) {
                startBattle();
            } else {
                putShips();
            }
        }
    }

    public void setScene(final String fileName) {
        setScene(fileName, null);
    }

    public void setScene(final String fileName, final Object controller) {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("layouts/" + fileName + ".fxml"));
        if(Objects.nonNull(controller)) {
            fxmlLoader.setController(controller);
        }
        try {
            primaryStage.getScene().setRoot(fxmlLoader.load());
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public Battle getBattle() {
        return battle;
    }

    public static void main(String[] args) {
        Application.launch();
    }
}
