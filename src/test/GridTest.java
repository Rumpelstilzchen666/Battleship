import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class GridTest {
    private static final int DEFAULT_SIZE = 10, DEFAULT_LEN = 1;
    private static final Grid.Direction DEFAULT_DIRECTION = Grid.Direction.RIGHT;
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
        for(int i = 0; i < DEFAULT_SIZE; i++) {
            for(int j = 0; j < DEFAULT_SIZE; j++) {
                if(grid[i][j] != Grid.CellState.EMPTY) {
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
        @CsvSource({"-1, -1", "-1, 0", "-1, 10", "0, -1", "0, 10", "10, -1", "10, 0", "10, 10"})
        void throwsExceptionWhenSternOutOfGrid(final int x, final int y) {
            assert (x == -1 || x == 0 || x == DEFAULT_SIZE) && (y == -1 || y == 0 || y == DEFAULT_SIZE);
            assertThrows(IndexOutOfBoundsException.class,
                    () -> grid.putProbableShip(x, y, DEFAULT_LEN, DEFAULT_DIRECTION));
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
        void throwsExceptionWhenProbableShipOutOfGrid(final Grid.Direction direction) {
            assertThrows(Grid.ShipLocationException.class, () -> putDefaultProbableShip(DEFAULT_SIZE + 1, direction));
        }

        @ParameterizedTest
        @EnumSource
        void doesNotThrowsExceptionWhenProbableShipInGrid(Grid.Direction direction) {
            assertDoesNotThrow(() -> putDefaultProbableShip(direction));
        }

        @ParameterizedTest
        @CsvSource({"1, RIGHT", "1, DOWN", "1, UP", "1, LEFT",
                "3, RIGHT", "3, DOWN", "3, UP", "3, LEFT",
                "10, RIGHT", "10, DOWN", "10, UP", "10, LEFT"})
        void putProbableShipInDifferentDirections(int len, Grid.Direction direction) throws Grid.ShipLocationException {
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
            void confirmProbableShip() throws Grid.ShipLocationException {
                assertTrue(grid.confirmProbableShip());
            }

            @Test
            void removeProbableShip() {
                grid.removeProbableShip();
            }

            @Test
            void gridNotEmpty() {
                assertFalse(isGridEmpty());
            }

            @Nested
            class AfterRemovingProbableShip {
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
            }
        }

        private boolean isGridEmpty() {
            return Arrays.deepEquals(grid.getGrid(), new Grid(DEFAULT_SIZE).getGrid());
        }

        private boolean putDefaultProbableShip(final Grid.Direction direction) throws Grid.ShipLocationException {
            return putDefaultProbableShip(DEFAULT_LEN, direction);
        }

        private boolean putDefaultProbableShip(final int len) throws Grid.ShipLocationException {
            return putDefaultProbableShip(len, DEFAULT_DIRECTION);
        }

        private boolean putDefaultProbableShip(final int len, final Grid.Direction direction)
                throws Grid.ShipLocationException {
            final int sternCoordinate = switch(direction) {
                case RIGHT, DOWN -> 0;
                case UP, LEFT -> DEFAULT_SIZE - 1;
            };
            return grid.putProbableShip(sternCoordinate, sternCoordinate, len, direction);
        }
    }
}