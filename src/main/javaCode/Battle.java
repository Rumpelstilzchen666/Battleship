package javaCode;

import java.util.Arrays;


public class Battle {
    public final Grid[] grid;
    public final ShipType[] shipTypes;
    public final int[][] nShips = new int[2][];
    public final String[] playersNames = new String[]{"Ярослав", "Тестировщик"};

    public Battle(final int size, final ShipType[] shipTypes) {
        grid = new Grid[]{new Grid(size), new Grid(size)};
        if(shipTypes == null) {
            throw new NullPointerException("shipTypes == null");
        }
        if(shipTypes.length == 0) {
            throw new IllegalArgumentException("shipTypes.length == 0");
        }
        this.shipTypes = shipTypes;
        nShips[0] = new int[this.shipTypes.length];
        Arrays.fill(nShips[0], 0);
        nShips[1] = nShips[0].clone();
    }

    public int[] getNShips(final int playerN) {
        return nShips[playerN];
    }

    public int[] getMaxNShips() {
        int[] maxNShips = new int[shipTypes.length];
        for(int i = 0; i < shipTypes.length; i++) {
            maxNShips[i] = shipTypes[i].n();
        }
        return maxNShips;
    }
}
