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

    public int getPlayerN(final boolean present) {
        return (zeroPlayer == present) ? 0 : 1;
    }

    public void nextPlayer() {
        zeroPlayer = !zeroPlayer;
    }

    public ShipType[] getShipTypes() {
        return shipTypes;
    }

    public String getPlayerName(final boolean present) {
        return playersNames[getPlayerN(present)];
    }

    public Grid getGrid(final boolean present) {
        return grid[getPlayerN(present)];
    }

    public int[] getNShips(final boolean present) {
        return nShips[getPlayerN(present)];
    }

    public ArrayList<Ship> getShips(final boolean present) {
        return (getPlayerN(present) == 0 ? ships0 : ships1);
    }

    public void addShip(final int shipTypeN, final Coordinate sternCoordinate, final Direction direction) {
        if(shipTypeN < 0 || shipTypeN >= shipTypes.length) {
            throw new IndexOutOfBoundsException("Ship type number: " + shipTypeN + ", Size: " + shipTypes.length);
        }
        if(getNShips(true)[shipTypeN] == shipTypes[shipTypeN].n()) {
            throw new IllegalStateException("Too many ships of this type");
        }
        getNShips(true)[shipTypeN]++;
        getShips(true).add(new Ship(shipTypes[shipTypeN], sternCoordinate, direction));
    }

    public Grid.FireResult fire(final Coordinate coordinate) throws Grid.SelectedCellException {
        return getGrid(false).fire(coordinate.col(), coordinate.row());
    }

    public Ship getShip(final Coordinate coordinate) {
        final Coordinate leftUpShipEndCoordinate = getGrid(false).getSternCoordinate(coordinate.col(), coordinate.row()),
                rightDownShipEndCoordinate = getGrid(false).getBowCoordinate(coordinate.col(), coordinate.row());
        for(Ship ship : getShips(false)) {
            if(ship.sternCoordinate().equals(leftUpShipEndCoordinate) ||
                    ship.sternCoordinate().equals(rightDownShipEndCoordinate)) {
                return ship;
            }
        }
        return null;
    }

    public record Ship(ShipType shipType, Coordinate sternCoordinate, Direction direction) {
        public Ship {
            if(shipType == null) {
                throw new NullPointerException();
            }
            if(direction == null) {
                direction = Direction.RIGHT;
            }
        }
    }
}
