module mirea.battleship {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.fasterxml.jackson.dataformat.xml;
    requires com.fasterxml.jackson.databind;


    exports mirea.battleship;
    exports mirea.battleship.Controllers;
    exports mirea.battleship.Backend;
    opens mirea.battleship to javafx.fxml;
    opens mirea.battleship.Controllers to javafx.fxml;
    opens mirea.battleship.Backend to com.fasterxml.jackson.databind;
}