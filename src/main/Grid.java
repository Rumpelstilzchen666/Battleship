import java.util.ArrayList;

public class Grid {
    private final CellState[][] grid;
    private final ArrayList<Coordinate> probableShipCoordinates = new ArrayList<>();
    private boolean probableShipOk;

    public Grid(int size) {
        if(size < 1) { throw new IllegalArgumentException("size(" + size + ") < 1"); }
        this.grid = new CellState[size][size];
        for(int row = 0; row < size; row++) {
            for(int col = 0; col < size; col++) {
                this.grid[row][col] = CellState.EMPTY;
            }
        }
    }

    public boolean putProbableShip(Coordinate sternCoordinate, int len, Direction dir) throws ShipLocationException {
        checkCoordinate(sternCoordinate);
        if(len < 1) {
            throw new IllegalArgumentException("length(" + len + ") < 1");
        }
        removeProbableShip();
        Coordinate[] shipCoordinates = getShipCoordinates(sternCoordinate, len, dir);
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
            } else if(grid[x + 1][y] == CellState.SHIP || grid[x - 1][y] == CellState.SHIP ||
                    grid[x][y + 1] == CellState.SHIP || grid[x][y - 1] == CellState.SHIP ||
                    !Settings.USE_CORNERS && (grid[x + 1][y + 1] == CellState.SHIP || grid[x + 1][y - 1] == CellState.SHIP ||
                            grid[x - 1][y + 1] == CellState.SHIP || grid[x - 1][y - 1] == CellState.SHIP)) {
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

    private void removeProbableShip() {
        for(Coordinate probableShipCoordinate : probableShipCoordinates) {
            grid[probableShipCoordinate.x()][probableShipCoordinate.y()] = CellState.EMPTY;
        }
        probableShipCoordinates.clear();
    }

    private Coordinate[] getShipCoordinates(Coordinate sternCoordinate, int len, Direction dir) {
        Coordinate[] shipCoordinates = new Coordinate[len];
        for(int i = 0; i < len; i++) {
            shipCoordinates[i] = switch(dir) {
                case UP -> new Coordinate(sternCoordinate.x() + i, sternCoordinate.y());
                case RIGHT -> new Coordinate(sternCoordinate.x(), sternCoordinate.y() + i);
                case DOWN -> new Coordinate(sternCoordinate.x() - i, sternCoordinate.y());
                case LEFT -> new Coordinate(sternCoordinate.x(), sternCoordinate.y() - i);
            };
        }
        return shipCoordinates;
    }

    public boolean confirmShip() throws ShipLocationException {
        if(!probableShipOk) {
            throw new ShipLocationException("Корабль расположен неправильно.");
        }
        for(Coordinate coordinate : probableShipCoordinates) {
            grid[coordinate.x()][coordinate.y()] = CellState.SHIP;
        }
        probableShipCoordinates.clear();
        return true;
    }

    private void checkCoordinate(Coordinate coordinate) {
        checkCoordinate(coordinate.x());
        checkCoordinate(coordinate.y());
    }

    private void checkCoordinate(int x) {
        if(x < 0 || x >= grid.length) {
            throw new IndexOutOfBoundsException("Coordinate: " + x + ", Size: " + grid.length);
        }
    }

    public enum Direction {
        UP, DOWN, LEFT, RIGHT
    }

    public enum CellState {
        EMPTY, MISS, AUREOLE, SHIP, PROBABLE_SHIP, HIT
    }

    public static class ShipLocationException extends Exception {
        public ShipLocationException(String message) {
            super(message);
        }
    }
}
