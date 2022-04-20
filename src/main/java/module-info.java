module mirea.battleship {
    requires javafx.controls;
    requires javafx.fxml;


    exports mirea.battleship;
    exports mirea.battleship.Controllers;
    opens mirea.battleship to javafx.fxml;
    opens mirea.battleship.Controllers to javafx.fxml;
}