public class Grid {
    public static final short EMPTY = 0, MISS = 1, HALO = 2, SHIP = 10, HIT = 11;
    private final short[][] grid;

    public Grid(int size) {
        if(size < 1) {
            throw new IllegalArgumentException("size(" + size + ") < 1");
        }
        this.grid = new short[size][size];
        for(int row = 0; row < size; row++) {
            for(int col = 0; col < size; col++) {
                this.grid[row][col] = EMPTY;
            }
        }
    }
}
