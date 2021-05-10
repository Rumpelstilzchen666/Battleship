package javaCode;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class ShipTypeTest {
    private static final int DEFAULT_N = 1;
    private static final int DEFAULT_LEN = 1;
    private static final String DEFAULT_NAME = "Test";

    @Test
    void throwsExceptionWhenNLessThen0() {
        assertThrows(IllegalArgumentException.class, () -> new ShipType(DEFAULT_NAME, -1, DEFAULT_LEN));
    }

    @Test
    void doesNotThrowsExceptionWhenNAtLeast0() {
        assertDoesNotThrow(() -> new ShipType(DEFAULT_NAME, 0, DEFAULT_LEN));
    }

    @Test
    void throwsExceptionWhenLengthLessThen1() {
        assertThrows(IllegalArgumentException.class, () -> new ShipType(DEFAULT_NAME, DEFAULT_N, 0));
    }

    @Test
    void doesNotThrowsExceptionWhenLengthAtLeast1() {
        assertDoesNotThrow(() -> new ShipType(DEFAULT_NAME, DEFAULT_N, 1));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "")
    void setsDefaultNameIfNeeded(String name) {
        assertEquals(new ShipType(name, DEFAULT_N, DEFAULT_LEN).name(), "1-палубный");
    }

    @Nested
    class returnsEnteredData {
        private final ShipType DEFAULT_SHIP_TYPE = new ShipType(DEFAULT_NAME, DEFAULT_N, DEFAULT_LEN);

        @Test
        void returnsEnteredName() {
            assertEquals(DEFAULT_SHIP_TYPE.name(), DEFAULT_NAME);
        }

        @Test
        void returnsEnteredN() {
            assertEquals(DEFAULT_SHIP_TYPE.n(), DEFAULT_N);
        }

        @Test
        void returnsEnteredLength() {
            assertEquals(DEFAULT_SHIP_TYPE.len(), DEFAULT_LEN);
        }
    }
}
