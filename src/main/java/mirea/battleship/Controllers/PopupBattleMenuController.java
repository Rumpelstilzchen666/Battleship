package mirea.battleship.Controllers;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.stage.Stage;
import mirea.battleship.Settings;

public class PopupBattleMenuController extends PopupMenuController {
    private boolean saved = false;

    public PopupBattleMenuController(final Stage menuStage, final EventHandler<ActionEvent> buttonSaveOnAction) {
        super(menuStage);
        final PopupMenuController popupOptionMenu = PopupOptionMenuController.init(this.menuStage,
                "Вы действительно хотите выйти?\nВсе несохранённые изменения будут утеряны.",
                new EventHandler[]{event -> {
                    hide();
                    Settings.getApp().finishGame();
                }, null});
        buttonOnActions = new EventHandler[]{
                actionEvent -> hide(),
                actionEvent -> {
                    if(buttonSaveOnAction != null) {
                        buttonSaveOnAction.handle((ActionEvent) actionEvent);
                        saved = true;
                    }
                },
                null,
                null,
                actionEvent -> {
                    if(saved) {
                        hide();
                        Settings.getApp().finishGame();
                    } else {
                        popupOptionMenu.show();
                    }
                }};
        // Другие варианты:          Вернуться к игре                                   Выйти в меню
        buttonLabels = new String[]{"Продолжить", "Сохранить", "Настройки", "Правила", "Главное меню"};
    }

    public static void init(final Stage primaryStage, final EventHandler<ActionEvent> buttonSaveOnAction) {
        final Stage menuStage = initStage(primaryStage);
        final PopupBattleMenuController controller = new PopupBattleMenuController(menuStage, buttonSaveOnAction);
        setOnEscapePressed(primaryStage, keyEvent -> controller.show());
        menuStage.setScene(initScene(controller));
    }

    @Override
    public void hide() {
        saved = false;
        super.hide();
    }
}
