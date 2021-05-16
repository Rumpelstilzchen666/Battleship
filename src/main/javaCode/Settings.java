package javaCode;

public class Settings {
    public static final boolean USE_CORNERS = false, SHOW_MY_AUREOLE = true, SHOW_ENEMY_AUREOLE = true;
    public static final Style style = Style.DARK;

    public enum Style {
        LIGHT("light_styles"), DARK("dark_styles");
        private final String filePath;

        Style(String fileName) {
            this.filePath = "/resources/" + fileName + ".css";
        }

        public String getFilePath() {
            return filePath;
        }
    }
}
