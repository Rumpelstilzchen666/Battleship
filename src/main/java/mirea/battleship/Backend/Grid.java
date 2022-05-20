package mirea.battleship.Backend;

import mirea.battleship.Settings;

import java.util.ArrayList;

/** Внутреннее представление игрового поля. */
public class Grid {
    /** Матрица grid используется для хранения информации о состоянии ячеек игрового поля. */
    private final CellState[][] grid;
    /** Массив shipBeingPutCoordinates используется для хранения координат, занимаемых устанавливаемым кораблём. */
    private Coordinate[] shipBeingPutCoordinates;

    /**
     * Создаёт пустое игровое поле размера {@code size}.
     *
     * @param size размер игрового поля.
     * @throws IllegalArgumentException если некорректный размер игрового поля ({@code size < 1}).
     */
    public Grid(final int size) {
        if(size < 1) { throw new IllegalArgumentException("size(" + size + ") < 1"); }
        this.grid = new CellState[size][size];
        for(int row = 0; row < size; row++) {
            for(int col = 0; col < size; col++) {
                this.grid[row][col] = CellState.EMPTY;
            }
        }
    }

    /**
     * Проверяет, что корабль можно установить на игровое поле.
     *
     * @param sternCol номер столбца, где расположена корма проверяемого корабля.
     * @param sternRow номер строки, где расположена корма проверяемого корабля.
     * @param len      длина проверяемого корабля.
     * @param dir      направление носа проверяемого корабля.
     * @return {@code true} если корабль можно установить на игровое поле.
     * @throws IndexOutOfBoundsException если строка или столбец, где расположена корма проверяемого корабля, вне
     *                                   игрового поля.
     * @throws IllegalArgumentException  если длина проверяемого корабля некорректна ({@code len < 1}).
     * @throws NullPointerException      если {@code dir == null}.
     * @throws ShipLocationException     если проверяемый корабль невозможно установить на игровое поле.
     */
    public boolean isOkPutShip(final int sternCol, final int sternRow, final int len, final Direction dir)
            throws ShipLocationException {
        checkCoordinate(sternCol);
        checkCoordinate(sternRow);
        if(len < 1) {
            throw new IllegalArgumentException("length(" + len + ") < 1");
        }
        if(dir == null) {
            throw new NullPointerException("direction == null");
        }
        final Coordinate[] shipCoordinates = getShipCoordinates(sternCol, sternRow, len, dir);
        boolean touchAnotherShip = false;
        int col, row;
        for(Coordinate shipCoordinate : shipCoordinates) {
            try {
                checkCoordinate(shipCoordinate);
            } catch(IndexOutOfBoundsException e) {
                throw new ShipLocationException("Корабль выходит за рамки поля.");
            }
            col = shipCoordinate.col();
            row = shipCoordinate.row();
            if(grid[row][col] == CellState.SHIP) {
                throw new ShipLocationException("Корабль пересекается с другим кораблём.");
            } else if((col < grid.length - 1 && grid[row][col + 1] == CellState.SHIP) ||
                    (col > 0 && grid[row][col - 1] == CellState.SHIP) ||
                    (row < grid.length - 1 && grid[row + 1][col] == CellState.SHIP) ||
                    (row > 0 && grid[row - 1][col] == CellState.SHIP) ||
                    !Settings.USE_CORNERS &&
                            ((col < grid.length - 1 && row < grid.length - 1 && grid[row + 1][col + 1] == CellState.SHIP) ||
                                    (col > 0 && row < grid.length - 1 && grid[row + 1][col - 1] == CellState.SHIP) ||
                                    (col < grid.length - 1 && row > 0 && grid[row - 1][col + 1] == CellState.SHIP) ||
                                    (col > 0 && row > 0 && grid[row - 1][col - 1] == CellState.SHIP))) {
                 touchAnotherShip = true;
            }
        }
        if(touchAnotherShip) {
            throw new ShipLocationException("Корабль касается другого корабля.");
        }
        shipBeingPutCoordinates = shipCoordinates;
        return true;
    }

    /**
     * Определяет координаты корабля на игровом поле.
     *
     * @param sternCol номер столбца, где расположена корма корабля.
     * @param sternRow номер строки, где расположена корма корабля.
     * @param len      длина корабля.
     * @param dir      направление носа корабля.
     * @return массив координат корабля.
     */
    public static Coordinate[] getShipCoordinates(final int sternCol, final int sternRow, final int len, final Direction dir) {
        final Coordinate[] shipCoordinates = new Coordinate[len];
        for(int i = 0; i < len; i++) {
            shipCoordinates[i] = switch(dir) {
                case UP -> new Coordinate(sternCol, sternRow - i);
                case RIGHT -> new Coordinate(sternCol + i, sternRow);
                case DOWN -> new Coordinate(sternCol, sternRow + i);
                case LEFT -> new Coordinate(sternCol - i, sternRow);
            };
        }
        return shipCoordinates;
    }


    /**
     * Устанавливает корабль на игровое поле.
     *
     * @param sternCol номер столбца, где расположена корма корабля.
     * @param sternRow номер строки, где расположена корма корабля.
     * @param len      длина корабля.
     * @param dir      направление носа корабля.
     * @return {@code true} если корабль успешно установлен на игровое поле.
     * @throws IndexOutOfBoundsException если строка или столбец, где расположена корма корабля, вне игрового поля.
     * @throws IllegalArgumentException  если длина корабля некорректна ({@code len < 1}).
     * @throws NullPointerException      если {@code dir == null}.
     * @throws ShipLocationException     если корабль невозможно установить на игровое поле.
     */
    public boolean putShip(final int sternCol, final int sternRow, final int len, final Direction dir)
            throws ShipLocationException {
        if(isOkPutShip(sternCol, sternRow, len, dir)) {
            if(shipBeingPutCoordinates == null) {
                throw new InternalError("Не сохранены координаты устанавливаемого корабля.");
            }
            for(Coordinate coordinate : shipBeingPutCoordinates) {
                grid[coordinate.row()][coordinate.col()] = CellState.SHIP;
            }
            shipBeingPutCoordinates = null;
            return true;
        }
        return false;
    }

    /**
     * Удаляет корабль с игрового поля.
     *
     * @param col номер столбца, где расположен корабль.
     * @param row номер строки, где расположен корабль.
     * @return {@code true} если корабль успешно удалён с игрового поля.
     * @throws SelectedCellException если на указанных координатах нет корабля.
     * @throws RemovalShipException  если корабль на указанных координатах невозможно удалить.
     */
    public boolean removeShip(final int col, final int row) throws SelectedCellException, RemovalShipException {
        checkCoordinate(col);
        checkCoordinate(row);
        if(!grid[row][col].isVessel()) {
            throw new SelectedCellException("На этой клетке нет корабля.");
        }

        final Coordinate sternCoordinate = getSternCoordinate(col, row);
        final ArrayList<Coordinate> shipCoordinates = new ArrayList<>();
        for(int shipCol = sternCoordinate.col() + 1;
                shipCol < grid.length && grid[sternCoordinate.row()][shipCol].isVessel(); shipCol++) {
            if(grid[sternCoordinate.row()][shipCol].isFired()) {
                throw new RemovalShipException("Невозможно удалить обстрелянный корабль.");
            }
            shipCoordinates.add(new Coordinate(shipCol, sternCoordinate.row()));
        }
        for(int shipRow = sternCoordinate.row();
                shipRow < grid.length && grid[shipRow][sternCoordinate.col()].isVessel(); shipRow++) {
            if(grid[shipRow][sternCoordinate.col()].isFired()) {
                throw new RemovalShipException("Невозможно удалить обстрелянный корабль.");
            }
            shipCoordinates.add(new Coordinate(sternCoordinate.col(), shipRow));
        }
        for(Coordinate shipCoordinate : shipCoordinates) {
            grid[shipCoordinate.row()][shipCoordinate.col()] = CellState.EMPTY;
        }
        return true;
    }

    /**
     * Определяет координату левого верхнего конца корабля.
     *
     * @param col номер столбца, где расположен корабль.
     * @param row номер строки, где расположен корабль.
     * @return координату левого верхнего конца корабля.
     */
    Coordinate getSternCoordinate(int col, int row) {
        while(col > 0 && grid[row][col - 1].isVessel()) {
            col--;
        }
        while(row > 0 && grid[row - 1][col].isVessel()) {
            row--;
        }
        return new Coordinate(col, row);
    }

    /**
     * Определяет координату правого нижнего конца корабля.
     *
     * @param col номер столбца, где расположен корабль.
     * @param row номер строки, где расположен корабль.
     * @return координату правого нижнего конца корабля.
     */
    Coordinate getBowCoordinate(int col, int row) {
        while(col < grid.length - 1 && grid[row][col + 1].isVessel()) {
            col++;
        }
        while(row < grid.length - 1 && grid[row + 1][col].isVessel()) {
            row++;
        }
        return new Coordinate(col, row);
    }

    /**
     * Вызывается, чтобы произвести выстрел по игровому полю.
     *
     * @param col номер столбца, куда производится выстрел.
     * @param row номер строки, куда производится выстрел.
     * @return результат выстрела.
     * @throws SelectedCellException если по указанным координатам невозможно произвести выстрел.
     */
    public FireResult fire(final int col, final int row) throws SelectedCellException {
        checkCoordinate(col);
        checkCoordinate(row);
        if(grid[row][col].isFired()) {
            throw new SelectedCellException("Эта клетка уже была обстреляна.");
        }
        if(grid[row][col] == CellState.AUREOLE) {
            throw new SelectedCellException("Эта клетка соседствует с кораблём, поэтому не может быть занята.");
        }
        if(grid[row][col].isVessel()) {
            grid[row][col] = CellState.HIT;
            return shipKilled(col, row) ? FireResult.SUNK : FireResult.HIT;
        } else {
            grid[row][col] = CellState.MISS;
            return FireResult.MISS;
        }
    }

    /**
     * Проверяет, уничтожен ли корабль обстрелянный корабль. Если да, то отмечает занимаемые им ячейки
     * {@link CellState CellState.SUNK}, а соседние - {@link CellState CellState.AUREOLE}.
     *
     * @param firedCol номер обстрелянного столбца.
     * @param firedRow номер обстрелянной строки.
     * @return {@code true} если корабль потоплен.
     */
    private boolean shipKilled(final int firedCol, final int firedRow) {
        final Coordinate sternCoordinate = getSternCoordinate(firedCol, firedRow);
        final int sternCol = sternCoordinate.col(), sternRow = sternCoordinate.row();

        int bowCol, bowRow;
        for(bowCol = sternCol; bowCol < grid.length && grid[sternRow][bowCol].isVessel(); bowCol++) {
            if(grid[sternRow][bowCol] != CellState.HIT) {
                return false;
            }
        }
        for(bowRow = sternRow + 1; bowRow < grid.length && grid[bowRow][sternCol].isVessel(); bowRow++) {
            if(grid[bowRow][sternCol] != CellState.HIT) {
                return false;
            }
        }

        if(!Settings.USE_CORNERS) {
            if(sternCol > 0) {
                if(sternRow > 0 && grid[sternRow - 1][sternCol - 1] == CellState.EMPTY) {
                    grid[sternRow - 1][sternCol - 1] = CellState.AUREOLE;
                }
                if(bowRow < grid.length && grid[bowRow][sternCol - 1] == CellState.EMPTY) {
                    grid[bowRow][sternCol - 1] = CellState.AUREOLE;
                }
            }
            if(bowCol < grid.length) {
                if(sternRow > 0 && grid[sternRow - 1][bowCol] == CellState.EMPTY) {
                    grid[sternRow - 1][bowCol] = CellState.AUREOLE;
                }
                if(bowRow < grid.length && grid[bowRow][bowCol] == CellState.EMPTY) {
                    grid[bowRow][bowCol] = CellState.AUREOLE;
                }
            }
        }

        for(int shipCol = sternCol; shipCol < bowCol; shipCol++) {
            if(sternRow > 0 && grid[sternRow - 1][shipCol] == CellState.EMPTY) {
                grid[sternRow - 1][shipCol] = CellState.AUREOLE;
            }
            grid[sternRow][shipCol] = CellState.SUNK;
            if(sternRow < grid.length - 1 && grid[sternRow + 1][shipCol] == CellState.EMPTY) {
                grid[sternRow + 1][shipCol] = CellState.AUREOLE;
            }
        }
        for(int shipRow = sternRow; shipRow < bowRow; shipRow++) {
            if(sternCol > 0 && grid[shipRow][sternCol - 1] == CellState.EMPTY) {
                grid[shipRow][sternCol - 1] = CellState.AUREOLE;
            }
            grid[shipRow][sternCol] = CellState.SUNK;
            if(sternCol < grid.length - 1 && grid[shipRow][sternCol + 1] == CellState.EMPTY) {
                grid[shipRow][sternCol + 1] = CellState.AUREOLE;
            }
        }

        if(bowCol < grid.length && grid[bowRow - 1][bowCol] == CellState.EMPTY) {
            grid[bowRow - 1][bowCol] = CellState.AUREOLE;
        }
        if(bowRow < grid.length && grid[bowRow][bowCol - 1] == CellState.EMPTY) {
            grid[bowRow][bowCol - 1] = CellState.AUREOLE;
        }
        return true;
    }

    /**
     * Проверяет, что {@code coordinate} в границах игрового поля.
     *
     * @param coordinate координаты ячейки.
     * @throws IndexOutOfBoundsException если по крайней мере одна из линий вне игрового поля.
     */
    private void checkCoordinate(final Coordinate coordinate) {
        checkCoordinate(coordinate.col());
        checkCoordinate(coordinate.row());
    }

    /**
     * Проверяет, что линия под номером {@code line} в границах игрового поля.
     *
     * @param line номер линии ячейки.
     * @throws IndexOutOfBoundsException если линия вне игрового поля ({@code line < 0 || line >= getSize()}).
     */
    private void checkCoordinate(final int line) {
        if(line < 0 || line >= grid.length) {
            throw new IndexOutOfBoundsException("Coordinate: " + line + ", Size: " + grid.length);
        }
    }

    /**
     * Возвращает размер игрового поля.
     *
     * @return длину стороны квадратного игрового поля.
     */
    public int getSize() {
        return grid.length;
    }

    /**
     * Возвращает игровое поле.
     *
     * @return матрицу с информацией о состоянии ячеек игрового поля.
     */
    public CellState[][] getGrid() {
        return grid;
    }

    /**
     * Выводит игровое поле в консоль в виде таблицы.
     *
     * @param mine {@code true} если игровое поле принадлежит игроку, для которого оно выводится, или {@code false} если
     *             игровое поле принадлежит противнику этого игрока.
     */
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

    /**
     * Выводит в консоль горизонтальную линию между строками игрового поля.
     *
     * @param edgeType тип линии.
     * @throws IllegalArgumentException если неизвестное значение {@link EdgeType edgeType}.
     */
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

    /** Результат выстрела, возвращаемый {@link Grid#fire}. */
    public enum FireResult {
        MISS, HIT, SUNK
    }

    /** Состояние ячеек игрового поля {@link Grid}. */
    public enum CellState {
        EMPTY, SHIP, MISS, HIT, SUNK, AUREOLE;

        public boolean isFired() {
            return this == MISS || this == HIT || this == SUNK;
        }

        public boolean isVessel() {
            return this == SHIP || this == HIT || this == SUNK;
        }
    }

    /** Тип линии, выводимой {@link #printEdge}. */
    private enum EdgeType {
        TOP, DOUBLE, CENTRAL, BOTTOM
    }

    public static class ShipLocationException extends Exception {
        public ShipLocationException(final String message) {
            super(message);
        }
    }

    public static class SelectedCellException extends Exception {
        public SelectedCellException(final String message) {
            super(message);
        }
    }

    public static class RemovalShipException extends Exception {
        public RemovalShipException(final String message) {
            super(message);
        }
    }
}
