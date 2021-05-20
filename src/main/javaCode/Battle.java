package javaCode;

import java.util.ArrayList;
import java.util.Arrays;


public class Battle {
    private final String[] playersNames = new String[]{"Ярослав", "Тестировщик"};
    private final Grid[] grid;
    private final ShipType[] shipTypes;
    private final int[][] nShips = new int[2][];
    private boolean zeroPlayer;

    public Battle(final int size, final ShipType[] shipTypes) {
        grid = new Grid[]{new Grid(size), new Grid(size)};
        if(shipTypes == null) {
            throw new NullPointerException("shipTypes == null");
        }
        if(shipTypes.length == 0) {
            throw new IllegalArgumentException("shipTypes.length == 0");
        }
        //TODO Проверить что корабли входят на поле
        this.shipTypes = shipTypes;
        nShips[0] = new int[this.shipTypes.length];
        nShips[1] = nShips[0].clone();
        zeroPlayer = true;
    }

    public int getPlayerN() {
        return zeroPlayer ? 0 : 1;
    }

    public void nextPlayer() {
        zeroPlayer = !zeroPlayer;
    }

    public ShipType[] getShipTypes() {
        return shipTypes;
    }

    public String getPlayerName() {
        return playersNames[getPlayerN()];
    }

    public Grid getGrid() {
        return grid[getPlayerN()];
    }

    public int[] getNShips() {
        return nShips[getPlayerN()];
    }
}
