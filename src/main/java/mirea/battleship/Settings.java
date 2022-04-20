package mirea.battleship;

public class Settings {
    public static final boolean USE_CORNERS = false, SHOW_MY_AUREOLE = true, SHOW_ENEMY_AUREOLE = true;
    public static final Style style = Style.DARK;
    static App app;
    static int cellSize = 50;

    public static App getApp() {
        return app;
    }

    public static int getCellSize() {
        return cellSize;
    }

    public enum Style {
        LIGHT("light_styles"), DARK("dark_styles");
        private final String filePath;

        Style(String fileName) {
            this.filePath = "/mirea/battleship/" + fileName + ".css";
        }

        public String getFilePath() {
            return filePath;
        }
    }
}
