package javaCode;

import java.util.ArrayList;
import java.util.Arrays;


public class Battle {
    private final String[] playersNames = new String[]{"Ярослав", "Тестировщик"};
    private final Grid[] grid;
    private final ShipType[] shipTypes;
    private final int[][] nShips = new int[2][];
    private final ArrayList<Ship> ships0;
    private final ArrayList<Ship> ships1;
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
        final int[] nShips = {0};
        Arrays.asList(this.shipTypes).forEach(shipType -> nShips[0] += shipType.n());
        ships0 = new ArrayList<>(nShips[0]);
        ships1 = new ArrayList<>(nShips[0]);
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

    private ArrayList<Ship> getShips() {
        return (getPlayerN() == 0 ? ships0 : ships1);
    }

    public void addShip(final int shipTypeN, final Coordinate sternCoordinate, final Direction direction) {
        if(shipTypeN < 0 || shipTypeN >= shipTypes.length) {
            throw new IndexOutOfBoundsException("Ship type number: " + shipTypeN + ", Size: " + shipTypes.length);
        }
        if(getNShips()[shipTypeN] == shipTypes[shipTypeN].n()) {
            throw new IllegalStateException("Too many ships of this type");
        }
        getNShips()[shipTypeN]++;
        getShips().add(new Ship(shipTypes[shipTypeN], sternCoordinate, direction));
    }

    public void fire(final int colN, final int rowN) throws Grid.ShipLocationException {
    }

    private record Ship(ShipType shipType, Coordinate sternCoordinate, Direction direction) {
        private Ship {
            if(shipType == null) {
                throw new NullPointerException();
            }
            if(direction == null) {
                direction = Direction.RIGHT;
            }
        }
    }
}
