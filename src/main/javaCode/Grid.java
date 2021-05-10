package javaCode;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class Grid {
    private final CellState[][] grid;
    private final ArrayList<Coordinate> probableShipCoordinates = new ArrayList<>();
    private boolean probableShipOk;

    public Grid(final int size) {
        if(size < 1) { throw new IllegalArgumentException("size(" + size + ") < 1"); }
        this.grid = new CellState[size][size];
        for(int row = 0; row < size; row++) {
            for(int col = 0; col < size; col++) {
                this.grid[row][col] = CellState.EMPTY;
            }
        }
    }

    public boolean putProbableShip(final int sternX, final int sternY, final int len, final Direction dir)
            throws ShipLocationException {
        checkCoordinate(sternX);
        checkCoordinate(sternY);
        if(len < 1) {
            throw new IllegalArgumentException("length(" + len + ") < 1");
        }
        if(dir == null) {
            throw new NullPointerException("direction == null");
        }
        removeProbableShip();
        final Coordinate[] shipCoordinates = getShipCoordinates(sternX, sternY, len, dir);
        probableShipCoordinates.ensureCapacity(len);
        probableShipOk = false; // Превентивная мера, чтобы не вставлять на месте каждой возможной ошибки
        boolean touchAnotherShip = false;
        int x, y;
        for(Coordinate shipCoordinate : shipCoordinates) {
            try {
                checkCoordinate(shipCoordinate);
            } catch(IndexOutOfBoundsException e) {
                throw new ShipLocationException("Корабль выходит за рамки поля.");
            }
            x = shipCoordinate.x();
            y = shipCoordinate.y();
            if(grid[x][y] == CellState.SHIP) {
                throw new ShipLocationException("Корабль пересекается с другим кораблём.");
            } else if((x < grid.length - 1 && grid[x + 1][y] == CellState.SHIP) ||
                    (x > 0 && grid[x - 1][y] == CellState.SHIP) ||
                    (y < grid.length - 1 && grid[x][y + 1] == CellState.SHIP) ||
                    (y > 0 && grid[x][y - 1] == CellState.SHIP) ||
                    !Settings.USE_CORNERS &&
                            ((x < grid.length - 1 && y < grid.length - 1 && grid[x + 1][y + 1] == CellState.SHIP) ||
                                    (x < grid.length - 1 && y > 0 && grid[x + 1][y - 1] == CellState.SHIP) ||
                                    (x > 0 && y < grid.length - 1 && grid[x - 1][y + 1] == CellState.SHIP) ||
                                    (x > 0 && y > 0 && grid[x - 1][y - 1] == CellState.SHIP))) {
                 touchAnotherShip = true;
            }
            grid[x][y] = CellState.PROBABLE_SHIP;
            probableShipCoordinates.add(shipCoordinate);
        }
        if(touchAnotherShip) {
            throw new ShipLocationException("Корабль касается другого корабля.");
        }
        return probableShipOk = true;
    }

    private Coordinate[] getShipCoordinates(final int sternX, final int sternY, final int len, final Direction dir) {
        final Coordinate[] shipCoordinates = new Coordinate[len];
        for(int i = 0; i < len; i++) {
            shipCoordinates[i] = switch(dir) {
                case UP -> new Coordinate(sternX - i, sternY);
                case RIGHT -> new Coordinate(sternX, sternY + i);
                case DOWN -> new Coordinate(sternX + i, sternY);
                case LEFT -> new Coordinate(sternX, sternY - i);
            };
        }
        return shipCoordinates;
    }

    public void removeProbableShip() {
        for(Coordinate probableShipCoordinate : probableShipCoordinates) {
            grid[probableShipCoordinate.x()][probableShipCoordinate.y()] = CellState.EMPTY;
        }
        probableShipCoordinates.clear();
    }

    public boolean confirmProbableShip() throws ShipLocationException {
        if(!probableShipCoordinates.isEmpty()) {
            if(!probableShipOk) {
                throw new ShipLocationException("Корабль расположен неправильно.");
            }
            for(Coordinate coordinate : probableShipCoordinates) {
                grid[coordinate.x()][coordinate.y()] = CellState.SHIP;
            }
            probableShipCoordinates.clear();
            return true;
        }
        return false;
    }

    public boolean removeShip(final int x, final int y) throws SelectedCellException, RemovalShipException {
        checkCoordinate(x);
        checkCoordinate(y);
        if(!grid[x][y].isVessel()) {
            throw new SelectedCellException("На этой клетке нет корабля.");
        }

        final Coordinate sternCoordinate = getSternCoordinate(x, y);
        final ArrayList<Coordinate> shipCoordinates = new ArrayList<>();
        for(int shipX = sternCoordinate.x(); shipX < grid.length && grid[shipX][sternCoordinate.y()].isVessel();
                shipX++) {
            if(grid[shipX][sternCoordinate.y()].isFired()) {
                throw new RemovalShipException("Невозможно удалить обстрелянный корабль.");
            }
            shipCoordinates.add(new Coordinate(shipX, sternCoordinate.y()));
        }
        for(int shipY = sternCoordinate.y() + 1; shipY < grid.length && grid[sternCoordinate.x()][shipY].isVessel();
                shipY++) {
            if(grid[sternCoordinate.x()][shipY].isFired()) {
                throw new RemovalShipException("Невозможно удалить обстрелянный корабль.");
            }
            shipCoordinates.add(new Coordinate(sternCoordinate.x(), shipY));
        }
        for(Coordinate shipCoordinate : shipCoordinates) {
            grid[shipCoordinate.x()][shipCoordinate.y()] = CellState.EMPTY;
        }
        return true;
    }

    private Coordinate getSternCoordinate(int x, int y) {
        while(x > 0 && grid[x - 1][y].isVessel()) {
            x--;
        }
        while(y > 0 && grid[x][y - 1].isVessel()) {
            y--;
        }
        return new Coordinate(x, y);
    }

    public boolean fire(final int x, final int y, final AtomicBoolean killed) throws SelectedCellException {
        checkCoordinate(x);
        checkCoordinate(y);
        if(grid[x][y] == CellState.PROBABLE_SHIP) {
            throw new IllegalStateException("Probable ship must be removed or confirmed");
        }
        if(grid[x][y].isFired()) {
            throw new SelectedCellException("Эта клетка уже была обстреляна.");
        }
        if(grid[x][y] == CellState.AUREOLE) {
            throw new SelectedCellException("Эта клетка соседствует с кораблём, поэтому не может быть занята.");
        }
        if(grid[x][y].isVessel()) {
            grid[x][y] = CellState.HIT;
            killed.set(shipKilled(x, y));
        } else {
            grid[x][y] = CellState.MISS;
            killed.set(false);
        }
        return grid[x][y] != CellState.MISS;
    }

    private boolean shipKilled(final int firedX, final int firedY) {
        final Coordinate sternCoordinate = getSternCoordinate(firedX, firedY);
        final int sternX = sternCoordinate.x(), sternY = sternCoordinate.y();

        int bowX, bowY;
        for(bowX = sternX; bowX < grid.length && grid[bowX][sternY].isVessel(); bowX++) {
            if(grid[bowX][sternY] != CellState.HIT) {
                return false;
            }
        }
        for(bowY = sternY + 1; bowY < grid.length && grid[sternX][bowY].isVessel(); bowY++) {
            if(grid[sternX][bowY] != CellState.HIT) {
                return false;
            }
        }

        if(!Settings.USE_CORNERS) {
            if(sternX > 0) {
                if(sternY > 0 && grid[sternX - 1][sternY - 1] == CellState.EMPTY) {
                    grid[sternX - 1][sternY - 1] = CellState.AUREOLE;
                }
                if(bowY < grid.length && grid[sternX - 1][bowY] == CellState.EMPTY) {
                    grid[sternX - 1][bowY] = CellState.AUREOLE;
                }
            }
            if(bowX < grid.length) {
                if(sternY > 0 && grid[bowX][sternY - 1] == CellState.EMPTY) {
                    grid[bowX][sternY - 1] = CellState.AUREOLE;
                }
                if(bowY < grid.length && grid[bowX][bowY] == CellState.EMPTY) {
                    grid[bowX][bowY] = CellState.AUREOLE;
                }
            }
        }

        for(int shipX = sternX; shipX < bowX; shipX++) {
            if(sternY > 0 && grid[shipX][sternY - 1] == CellState.EMPTY) {
                grid[shipX][sternY - 1] = CellState.AUREOLE;
            }
            grid[shipX][sternY] = CellState.SUNK;
            if(sternY < grid.length - 1 && grid[shipX][sternY + 1] == CellState.EMPTY) {
                grid[shipX][sternY + 1] = CellState.AUREOLE;
            }
        }
        for(int shipY = sternY; shipY < bowY; shipY++) {
            if(sternX > 0 && grid[sternX - 1][shipY] == CellState.EMPTY) {
                grid[sternX - 1][shipY] = CellState.AUREOLE;
            }
            grid[sternX][shipY] = CellState.SUNK;
            if(sternX < grid.length - 1 && grid[sternX + 1][shipY] == CellState.EMPTY) {
                grid[sternX + 1][shipY] = CellState.AUREOLE;
            }
        }

        if(bowX < grid.length && grid[bowX][bowY - 1] == CellState.EMPTY) {
            grid[bowX][bowY - 1] = CellState.AUREOLE;
        }
        if(bowY < grid.length && grid[bowX - 1][bowY] == CellState.EMPTY) {
            grid[bowX - 1][bowY] = CellState.AUREOLE;
        }
        return true;
    }

    private void checkCoordinate(final Coordinate coordinate) {
        checkCoordinate(coordinate.x());
        checkCoordinate(coordinate.y());
    }

    private void checkCoordinate(final int x) {
        if(x < 0 || x >= grid.length) {
            throw new IndexOutOfBoundsException("Coordinate: " + x + ", Size: " + grid.length);
        }
    }

    public int getSize() {
        return grid.length;
    }

    public CellState[][] getGrid() {
        return grid;
    }

    public void printGrid(final boolean mine) {
        final char wall = '│';
        printEdge(EdgeType.TOP);
        //Нумерация стобцов
        System.out.print(wall + "   " + '║'); //Уголок
        for(int j = 0; j < getSize(); j++) {
            System.out.print(" " + (char) ('А' + j) + ' ' + wall); // Буква столбца
        }
        printEdge(EdgeType.DOUBLE);

        for(int i = 0; i < getSize(); i++) {
            System.out.print(wall + ((i < 9) ? " " : "") + (i + 1) + ((i < 99) ? " " : "") + '║'); //Номер строки
            for(int j = 0; j < getSize(); j++) {
                System.out.print(' ' + switch(grid[i][j]) {
                    case EMPTY -> "  ";
                    case PROBABLE_SHIP -> probableShipOk ? " ⃞ " : "￯ ";
                    case SHIP -> mine ? "■ " : "  ";
                    case MISS -> "○  ";
                    case HIT -> "❌ ";
                    case AUREOLE -> ((mine && Settings.SHOW_MY_AUREOLE) || (!mine && Settings.SHOW_ENEMY_AUREOLE)) ?
                            "◌ " : "  ";
                    case SUNK -> "⛝ ";
                } + wall);
            }
            printEdge(((i == getSize() - 1) ? EdgeType.BOTTOM : EdgeType.CENTRAL));
        }
    }

    private void printEdge(final EdgeType edgeType) {
        final char left, central, right, floor, delimiter;
        switch(edgeType) {
            case CENTRAL -> {
                left = '├';
                central = '┼';
                right = '┤';
                floor = '─';
                delimiter = '╫';
            }
            case TOP -> {
                left = '┌';
                central = '┬';
                right = '┐';
                floor = '─';
                delimiter = '╥';
            }
            case DOUBLE -> {
                left = '╞';
                central = '╪';
                right = '╡';
                floor = '═';
                delimiter = '╬';
            }
            case BOTTOM -> {
                left = '└';
                central = '┴';
                right = '┘';
                floor = '─';
                delimiter = '╨';
            }
            default -> throw new IllegalArgumentException("Unexpected value: " + edgeType);
        }

        System.out.print((edgeType == EdgeType.TOP ? "" : "\n") + left + floor + floor + floor + delimiter);
        for(int i = 0; i < getSize(); i++) {
            System.out.print("" + floor + floor + floor + ((i == getSize() - 1) ? "" : central));
        }
        System.out.print(right + "\n");
    }

    public enum Direction {
        UP, RIGHT, DOWN, LEFT
    }

    public enum CellState {
        EMPTY, PROBABLE_SHIP, SHIP, MISS, HIT, SUNK, AUREOLE;

        public boolean isFired() {
            return this == MISS || this == HIT || this == SUNK;
        }

        public boolean isVessel() {
            return this == SHIP || this == HIT || this == SUNK;
        }
    }

    private enum EdgeType {
        TOP, DOUBLE, CENTRAL, BOTTOM
    }

    private record Coordinate(int x, int y) { }

    public static class ShipLocationException extends Exception {
        public ShipLocationException(String message) {
            super(message);
        }
    }

    public static class SelectedCellException extends Exception {
        public SelectedCellException(String message) {
            super(message);
        }
    }

    public static class RemovalShipException extends Exception {
        public RemovalShipException(String message) {
            super(message);
        }
    }
}
