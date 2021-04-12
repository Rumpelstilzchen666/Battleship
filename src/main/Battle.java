import java.util.Arrays;


public class Battle {
    public final Grid grid0, grid1;
    public final ShipType[] shipTypes;
    public final int[] nShips0, nShips1;

    public Battle(int size, ShipType[] shipTypes) {
        this.grid0 = new Grid(size);
        this.grid1 = new Grid(size);
        if(shipTypes == null || shipTypes.length == 0) { throw new IllegalArgumentException("ships.length == 0"); }
        this.shipTypes = shipTypes;
        nShips0 = new int[this.shipTypes.length];
        Arrays.fill(nShips0, 0);
        nShips1 = nShips0.clone();
    }

    public int[] getNShips0() {
        return nShips0;
    }

    public int[] getNShips1() {
        return nShips1;
    }

    public int[] getMaxNShips() {
        int[] maxNShips = new int[shipTypes.length];
        for(int i = 0; i < shipTypes.length; i++) {
            maxNShips[i] = shipTypes[i].n();
        }
        return maxNShips;
    }
}
