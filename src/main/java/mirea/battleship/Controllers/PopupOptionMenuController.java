package mirea.battleship.Controllers;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.stage.Stage;
import mirea.battleship.Settings;

import java.net.URL;
import java.util.ResourceBundle;

public class PopupOptionMenuController extends PopupMenuController {
    private final String headerText;

    public PopupOptionMenuController(final Stage menuStage, final String headerText,
            final EventHandler<ActionEvent>[] buttonOnActions) {
        super(menuStage);
        this.headerText = headerText;
        this.buttonOnActions = new EventHandler[]{actionEvent -> hide(), actionEvent -> hide()};
        if(buttonOnActions != null) {
            for(int buttonN = 0; buttonN < Math.min(2, buttonOnActions.length); buttonN++) {
                if(buttonOnActions[buttonN] != null) {
                    final int finalButtonN = buttonN;
                    this.buttonOnActions[buttonN] = actionEvent -> {
                        hide();
                        buttonOnActions[finalButtonN].handle(actionEvent);
                    };
                }
            }
        }
        buttonLabels = new String[]{"Да", "Нет"};
    }

    public static PopupOptionMenuController init(final Stage primaryStage, final String headerText,
            final EventHandler<ActionEvent>[] buttonOnActions) {
        final Stage menuStage = initStage(primaryStage);
        final PopupOptionMenuController controller = new PopupOptionMenuController(menuStage, headerText,
                buttonOnActions);
        menuStage.setScene(initScene(controller));
        return controller;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        GridUI.prepareMenuGrid(popupMenuGrid, buttonLabels, buttonOnActions, 0, 1);
        header.setText(headerText);
        header.setStyle("-fx-font-size: " + Settings.getCellSize() / 1.5 + ';');
    }
}
