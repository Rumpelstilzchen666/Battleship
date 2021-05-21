package javaCode;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Arrays;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class GridTest {
    private static final int DEFAULT_SIZE = 10, DEFAULT_LEN = 1;
    private static final Direction DEFAULT_DIRECTION = Direction.RIGHT;
    private static final Integer[][] coordinatesOutOfGrid = new Integer[][] {
            {          -1,           -1}, {          -1,            0}, {          -1, DEFAULT_SIZE},
            {           0,           -1},                               {           0, DEFAULT_SIZE},
            {DEFAULT_SIZE,           -1}, {DEFAULT_SIZE,            0}, {DEFAULT_SIZE, DEFAULT_SIZE}
    };
    private Grid grid;

    @Test
    void throwsExceptionWhenSizeLessThen1() {
        assertThrows(IllegalArgumentException.class, () -> new Grid(0));
    }

    @Test
    void doesNotThrowsExceptionWhenSizeAtLeast1() {
        assertDoesNotThrow(() -> new Grid(1));
    }

    @Test
    void newGridEmpty() {
        Grid.CellState[][] grid = new Grid(DEFAULT_SIZE).getGrid();
        boolean gridEmpty = true;
        for(int row = 0; row < DEFAULT_SIZE; row++) {
            for(int col = 0; col < DEFAULT_SIZE; col++) {
                if(grid[row][col] != Grid.CellState.EMPTY) {
                    gridEmpty = false;
                    break;
                }
            }
        }
        assertTrue(gridEmpty);
    }

    @Nested
    class WhenNew {
        @BeforeEach
        void createNewGrid() {
            grid = new Grid(DEFAULT_SIZE);
        }

        @ParameterizedTest
        @MethodSource("coordinatesOutOfGrid")
        void throwsExceptionWhenSternOutOfGrid(final int col, final int row) {
            assertThrows(IndexOutOfBoundsException.class,
                    () -> grid.putProbableShip(col, row, DEFAULT_LEN, DEFAULT_DIRECTION));
        }

        private static Stream<Integer[]> coordinatesOutOfGrid() {
            return Stream.of(coordinatesOutOfGrid);
        }

        @Test
        void throwsExceptionWhenLengthLessThen1() {
            assertThrows(IllegalArgumentException.class, () -> putDefaultProbableShip(0));
        }

        @Test
        void doesNotThrowsExceptionWhenLengthAtLeast1() {
            assertDoesNotThrow(() -> putDefaultProbableShip(1));
        }

        @ParameterizedTest
        @EnumSource
        void throwsExceptionWhenProbableShipOutOfGrid(final Direction direction) {
            assertThrows(Grid.ShipLocationException.class, () -> putDefaultProbableShip(DEFAULT_SIZE + 1, direction));
        }

        @ParameterizedTest
        @EnumSource
        void doesNotThrowsExceptionWhenProbableShipInGrid(Direction direction) {
            assertDoesNotThrow(() -> putDefaultProbableShip(direction));
        }

        @ParameterizedTest
        @CsvSource({"          1, RIGHT", "              1, DOWN", "              1, UP", "              1, LEFT",
                "              3, RIGHT", "              3, DOWN", "              3, UP", "              3, LEFT",
                DEFAULT_SIZE + ", RIGHT", DEFAULT_SIZE + ", DOWN", DEFAULT_SIZE + ", UP", DEFAULT_SIZE + ", LEFT"})
        void putProbableShipInDifferentDirections(int len, Direction direction) throws Grid.ShipLocationException {
            final int sternCoordinate = switch(direction) {
                case RIGHT, DOWN -> 0;
                case UP, LEFT -> DEFAULT_SIZE - 1;
            };
            grid.putProbableShip(sternCoordinate, sternCoordinate, len, direction);
            Grid.CellState[][] correctGrid = new Grid(DEFAULT_SIZE).getGrid();
            for(int i = 0; i < len; i++) {
                switch(direction) {
                    case UP    -> correctGrid[sternCoordinate - i][sternCoordinate] = Grid.CellState.PROBABLE_SHIP;
                    case RIGHT -> correctGrid[sternCoordinate][sternCoordinate + i] = Grid.CellState.PROBABLE_SHIP;
                    case DOWN  -> correctGrid[sternCoordinate + i][sternCoordinate] = Grid.CellState.PROBABLE_SHIP;
                    case LEFT  -> correctGrid[sternCoordinate][sternCoordinate - i] = Grid.CellState.PROBABLE_SHIP;
                }
            }
            assertTrue(Arrays.deepEquals(grid.getGrid(), correctGrid));
        }

        @ParameterizedTest
        @ValueSource(ints = {DEFAULT_LEN, DEFAULT_SIZE})
        void putProbableShip(final int len) throws Grid.ShipLocationException {
            assertTrue(putDefaultProbableShip(len));
        }

        @Test
        void confirmProbableShip() throws Grid.ShipLocationException {
            assertFalse(grid.confirmProbableShip());
        }

        @Test
        void removeProbableShip() {
            grid.removeProbableShip();
        }

        @Nested
        class AfterPuttingProbableShip {
            @BeforeEach
            void putProbableShip() throws Grid.ShipLocationException {
                putDefaultProbableShip(DEFAULT_SIZE);
            }

            @Test
            void gridNotEmpty() {
                assertFalse(isGridEmpty());
            }

            @Test
            void removeProbableShip() {
                grid.removeProbableShip();
            }

            @Nested
            class AfterRemovalProbableShip {
                @BeforeEach
                void removeProbableShip() {
                    grid.removeProbableShip();
                }

                @Test
                void confirmProbableShip() throws Grid.ShipLocationException {
                    assertFalse(grid.confirmProbableShip());
                }

                @Test
                void removeProbableShipAgain() {
                    grid.removeProbableShip();
                }

                @Test
                void gridEmpty() {
                    assertTrue(isGridEmpty());
                }
            }

            @Test
            void confirmProbableShip() throws Grid.ShipLocationException {
                assertTrue(grid.confirmProbableShip());
            }

            @Nested
            class AfterConfirmationProbableShip {
                @BeforeEach
                void confirmProbableShip() throws Grid.ShipLocationException {
                    grid.confirmProbableShip();
                }

                @Test
                void confirmProbableShipAgain() throws Grid.ShipLocationException {
                    assertFalse(grid.confirmProbableShip());
                }

                @Test
                void removeProbableShip() {
                    grid.removeProbableShip();
                }

                @Test
                void gridNotEmpty() {
                    assertFalse(isGridEmpty());
                }

                @Test
                void removeShip() throws Grid.SelectedCellException, Grid.RemovalShipException {
                    assertTrue(grid.removeShip(0, 0));
                }

                @Nested
                class RemovalShip {
                    @BeforeEach
                    void newGrid() {
                        grid = new Grid(DEFAULT_SIZE);
                    }

                    @ParameterizedTest
                    @MethodSource("coordinatesOutOfGrid")
                    void throwsExceptionWhenCellCoordinatesOutOfGrid(final int col, final int row) {
                        assertThrows(IndexOutOfBoundsException.class, () -> grid.removeShip(col, row));
                    }

                    private static Stream<Integer[]> coordinatesOutOfGrid() {
                        return Stream.of(coordinatesOutOfGrid);
                    }

                    @ParameterizedTest
                    @EnumSource
                    void throwsExceptionIfCellNotVessel(Grid.CellState cellState) {
                        if(!cellState.isVessel()) {
                            grid.getGrid()[0][0] = cellState;
                            assertThrows(Grid.SelectedCellException.class, () -> grid.removeShip(0,0));
                        }
                    }

                    @ParameterizedTest
                    @CsvSource({"          1,  DOWN,                          0,                     0",
                            "              1,  LEFT, " + (DEFAULT_SIZE - 1) + ", " + (DEFAULT_SIZE - 1),
                            DEFAULT_SIZE + ",  DOWN,                          0,                     0",
                            DEFAULT_SIZE + ",  DOWN,                          0,                     3",
                            DEFAULT_SIZE + ",  DOWN,                          0, " + (DEFAULT_SIZE - 1),
                            DEFAULT_SIZE + ", RIGHT,                          7,                     0",
                            DEFAULT_SIZE + ", RIGHT, " + (DEFAULT_SIZE - 1) + ",                     0"})
                    void removeShip(final int len, final Direction dir, final int col, final int row)
                            throws Grid.ShipLocationException, Grid.SelectedCellException, Grid.RemovalShipException {
                        putDefaultProbableShip(len, dir);
                        grid.confirmProbableShip();
                        grid.removeShip(col, row);
                        assertTrue(isGridEmpty());
                    }

                    @ParameterizedTest
                    @ValueSource(ints = {DEFAULT_LEN, DEFAULT_SIZE})
                    void throwsExceptionIfRemoveFiredShip(final int len)
                            throws Grid.ShipLocationException, Grid.SelectedCellException {
                        putDefaultProbableShip(len);
                        grid.confirmProbableShip();
                        grid.fire(0, 0);
                        assertThrows(Grid.RemovalShipException.class, () -> grid.removeShip(0,0));
                    }
                }

                @Test
                void fire() throws Grid.SelectedCellException {
                    assertEquals(grid.fire(0, 0), Grid.FireResult.HIT);
                }

                @ParameterizedTest
                @MethodSource("coordinatesOutOfGrid")
                void throwsExceptionWhenFireOutOfGrid(final int col, final int row) {
                    assertThrows(IndexOutOfBoundsException.class, () -> grid.removeShip(col, row));
                }

                private static Stream<Integer[]> coordinatesOutOfGrid() {
                    return Stream.of(coordinatesOutOfGrid);
                }

                @ParameterizedTest
                @EnumSource
                void throwsExceptionIfFireToIncorrectCell(Grid.CellState cellState) {
                    grid.getGrid()[DEFAULT_SIZE - 1][DEFAULT_SIZE - 1] = cellState;
                    switch(cellState) {
                        case PROBABLE_SHIP -> assertThrows(IllegalStateException.class,
                                () -> grid.fire(DEFAULT_SIZE - 1,DEFAULT_SIZE - 1));
                        case MISS, HIT, SUNK, AUREOLE -> assertThrows(Grid.SelectedCellException.class,
                                () -> grid.fire(DEFAULT_SIZE - 1,DEFAULT_SIZE - 1));
                    }
                }

                @Nested
                class TestFireMethod {
                    private static Grid grid = new Grid(10);

                    @BeforeAll
                    static void putShips() throws Grid.ShipLocationException {
                        final int[] sternCols = new int[]{0, 3, 9, 9, 0, 4, 6, 2, 9};
                        final int[] sternRows = new int[]{0, 3, 9, 0, 9, 6, 4, 9, 2};
                        final int[] lens      = new int[]{1, 1, 1, 1, 1, 4, 4, 6, 6};
                        final Direction[] directions =
                                new Direction[]{Direction.UP, Direction.UP, Direction.UP,
                                        Direction.UP, Direction.UP, Direction.LEFT, Direction.UP,
                                        Direction.RIGHT, Direction.DOWN};
                        for(int i = 0; i < sternRows.length; i++) {
                            grid.putProbableShip(sternCols[i], sternRows[i], lens[i], directions[i]);
                            grid.confirmProbableShip();
                        }
                        grid.printGrid(true);
                    }

                    @ParameterizedTest
                    @CsvSource({
                            "0, 0, SUNK",
                            "2, 2, MISS",
                            "2, 3, MISS",
                            "2, 4, MISS",
                            "3, 2, MISS",
                            "3, 4, MISS",
                            "4, 2, MISS",
                            "4, 3, MISS",
                            "4, 4, MISS",
                            "3, 3, SUNK",
                            "6, 6, MISS",
                            "9, 9, SUNK",
                            "0, 9, SUNK",
                            "9, 0, SUNK",
                            "1, 6,  HIT",
                            "2, 6,  HIT",
                            "3, 6,  HIT",
                            "4, 6, SUNK",
                            "6, 1,  HIT",
                            "6, 2,  HIT",
                            "6, 3,  HIT",
                            "6, 4, SUNK",
                            "2, 9,  HIT",
                            "4, 9,  HIT",
                            "6, 9,  HIT"
                    })
                    void assertFireResult(final int col, final int row, final Grid.FireResult expectedResult) throws Grid.SelectedCellException {
                        assertEquals(grid.fire(col, row), expectedResult);
                    }

                    @AfterAll
                    static void printGrid() {
                        grid.printGrid(true);
                        grid.printGrid(false);
                    }
                }
            }
        }

        private boolean isGridEmpty() {
            return Arrays.deepEquals(grid.getGrid(), new Grid(DEFAULT_SIZE).getGrid());
        }

        private boolean putDefaultProbableShip(final Direction direction) throws Grid.ShipLocationException {
            return putDefaultProbableShip(DEFAULT_LEN, direction);
        }

        private boolean putDefaultProbableShip(final int len) throws Grid.ShipLocationException {
            return putDefaultProbableShip(len, DEFAULT_DIRECTION);
        }

        private boolean putDefaultProbableShip(final int len, final Direction direction)
                throws Grid.ShipLocationException {
            final int sternCoordinate = switch(direction) {
                case RIGHT, DOWN -> 0;
                case UP, LEFT -> DEFAULT_SIZE - 1;
            };
            return grid.putProbableShip(sternCoordinate, sternCoordinate, len, direction);
        }
    }
}