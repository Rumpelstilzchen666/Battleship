package mirea.battleship;

public class IllegalXMLException extends Exception {
    public IllegalXMLException(final String message) {
        super(message);
    }

    public IllegalXMLException(String message, Throwable cause) {
        super(message, cause);
    }
}
