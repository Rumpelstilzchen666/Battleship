package mirea.battleship;

public class Settings {
    public static final int DEFAULT_SIZE = 10;
    public static final boolean USE_CORNERS = false, SHOW_MY_AUREOLE = true, SHOW_ENEMY_AUREOLE = true;
    public static final Style style = Style.DARK;
    private static final String PROJECT_DIR = "C:\\Users\\yaros\\YandexDisk\\МИРЭА\\Предметы\\СиПИ\\Практические работы\\";
    public static final String BATTLE_IN_FILE_PATH = PROJECT_DIR + "Battleship\\saves\\battles\\battle_ok.xml";
    public static final String BATTLE_OUT_FILE_PATH = PROJECT_DIR + "battle.xml";
    public static final String BATTLE_SET_IN_FILE_PATH = PROJECT_DIR + "Battleship\\saves\\battle sets\\ruWiki.xml";
    public static final String BATTLE_SET_OUT_FILE_PATH = PROJECT_DIR + "battle_set.xml";
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
