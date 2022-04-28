package mirea.battleship.Backend;

public record BattleSet(int gridSize, ShipType[] shipTypes) {
    public BattleSet {
        if(gridSize < 1) {
            throw new IllegalArgumentException("gridSize(" + gridSize + ") < 1");
        }
        if(shipTypes == null) {
            shipTypes = new ShipType[0];
        }
    }
}
