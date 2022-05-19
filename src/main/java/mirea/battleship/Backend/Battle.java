package mirea.battleship.Backend;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import mirea.battleship.IllegalXMLException;
import mirea.battleship.XMLTools;

import java.util.*;

public class Battle {
    private final String[] playersNames = new String[]{"Ярослав", "Тестировщик"};
    private final Grid[] grid;
    private final ShipType[] shipTypes;
    private final int[][] nShips = new int[2][];
    private final ArrayList<Ship> ships0;
    private final ArrayList<Ship> ships1;
    private final ArrayList<Coordinate> firedCoordinates = new ArrayList<>();
    private boolean zeroPlayer;

    public Battle(final BattleSet battleSet) {
        grid = new Grid[]{new Grid(battleSet.gridSize()), new Grid(battleSet.gridSize())};
        if(battleSet.shipTypes() == null) {
            throw new NullPointerException("shipTypes == null");
        }
        if(battleSet.shipTypes().length == 0) {
            throw new IllegalArgumentException("shipTypes.length == 0");
        }
        //TODO Проверить что корабли входят на поле
        this.shipTypes = battleSet.shipTypes();
        nShips[0] = new int[this.shipTypes.length];
        nShips[1] = nShips[0].clone();
        final int sumNShips = getSumNShips();
        ships0 = new ArrayList<>(sumNShips);
        ships1 = new ArrayList<>(sumNShips);
        zeroPlayer = true;
    }

    public Battle(final Map<String, Object> XMLMap) throws IllegalXMLException, JsonProcessingException {
        this(BattleSet.getFromXMLMap(XMLTools.getMapFromXMLMap(XMLMap, "BattleSet")));
        setPlayersName(true, (String) XMLMap.get("Player1Name"));
        setPlayersName(false, (String) XMLMap.get("Player2Name"));

        addShipsFromXMLMap(XMLMap, "Player1Ships");
        if(!allShipsArranged(true)) {
            //TODO Костыль, надо доработать ArrangeShipsSceneController для отображения частично расставленных кораблей
            getShips(true).clear();
            grid[0] = new Grid(grid[1].getSize());
            nShips[0] = new int[shipTypes.length];
            return;
        }
        nextPlayer();
        addShipsFromXMLMap(XMLMap, "Player2Ships");
        if(!allShipsArranged(true)) {
            getShips(true).clear();
            grid[1] = new Grid(grid[1].getSize());
            nShips[1] = new int[shipTypes.length];
            return;
        }
        nextPlayer();

        for(Coordinate coordinate : XMLTools.reXML(XMLMap.get("FiredCoordinates"), Coordinate[].class)) {
            try {
                if(fire(coordinate) == Grid.FireResult.MISS) {
                    nextPlayer();
                }
            } catch(Grid.SelectedCellException e) {
                throw new IllegalXMLException(
                        "Неверные координаты [" + coordinate.col() + ", " + coordinate.row() + "] в " +
                                XMLTools.angleBrc("FiredCoordinates") + '.', e);
            }
        }
    }

    private void addShipsFromXMLMap(final Map<String, Object> XMLMap, final String mapKey)
            throws JsonProcessingException, IllegalXMLException {
        for(Map<String, Object> shipMap : (ArrayList<Map<String, Object>>) XMLTools.reXML(XMLMap.get(mapKey),
                ArrayList.class)) {
            final Ship ship = Ship.getFromXMLMap(shipMap);
            if(getShipTypeN(ship.shipType()) < 0) {
                throw new IllegalXMLException(
                        "Несуществующий тип корабля " + ship.shipType() + " в " + XMLTools.angleBrc(mapKey) + '.');
            }
            try {
                getGrid(true).putProbableShip(ship.sternCoordinate().col(), ship.sternCoordinate().row(),
                        ship.shipType().len(), ship.direction());
                getGrid(true).confirmProbableShip();
            } catch(Grid.ShipLocationException e) {
                throw new IllegalXMLException(
                        "Неверные координаты корабля " + ship + " в " + XMLTools.angleBrc(mapKey) + '.', e);
            }
            addShip(getShipTypeN(ship.shipType()), ship.sternCoordinate(), ship.direction());
        }
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

    public void setPlayersName(final boolean present, final String name) {
        if(name != null && !name.isBlank()) {
            playersNames[getPlayerN(present)] = name.trim();
        }
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
        Grid.FireResult fireResult = getGrid(false).fire(coordinate.col(), coordinate.row());
        if(fireResult == Grid.FireResult.SUNK) {
            getNShips(false)[getShipTypeN(getShip(coordinate).shipType())]--;
        }
        firedCoordinates.add(coordinate);
        return fireResult;
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

    public boolean allShipsArranged(final boolean present) {
        return getShips(present).size() == getSumNShips();
    }

    private int getSumNShips() {
        final int[] nShips = {0};
        Arrays.asList(this.shipTypes).forEach(shipType -> nShips[0] += shipType.n());
        return nShips[0];
    }

    private int getShipTypeN(final ShipType shipType) {
        for(int shipTypeN = 0; shipTypeN < shipTypes.length; shipTypeN++) {
            if(shipTypes[shipTypeN].equals(shipType)) {
                return shipTypeN;
            }
        }
        return -1;
    }

    public Map<String, Object> getXMLMap() {
        final Map<String, Object> battle = new LinkedHashMap<>(6, 1.0F);
        battle.put("Player1Name", playersNames[0]);
        battle.put("Player2Name", playersNames[1]);
        battle.put("BattleSet", new BattleSet(grid[0].getSize(), shipTypes));
        battle.put("Player1Ships", Collections.singletonMap("Ship", ships0));
        battle.put("Player2Ships", Collections.singletonMap("Ship", ships1));
        battle.put("FiredCoordinates", Collections.singletonMap("Coordinate", firedCoordinates));
        return battle;
    }

    @Override
    public String toString() {
        return "Battle{" + "playersNames=" + Arrays.toString(playersNames) + ", grid=" + Arrays.toString(grid) +
                ", shipTypes=" + Arrays.toString(shipTypes) + ", nShips0=" + Arrays.toString(nShips[0]) +
                ", nShips1=" + Arrays.toString(nShips[1]) + ", ships[0=" + ships0 + ", 1=" + ships1 + ']' +
                ", firedCoordinates=" + firedCoordinates + ", zeroPlayer=" + zeroPlayer + '}';
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Ship(ShipType shipType, Coordinate sternCoordinate, Direction direction) {
        public Ship {
            if(shipType == null || sternCoordinate == null || direction == null) {
                throw new NullPointerException();
            }
        }

        public static Ship getFromXMLMap(final Map<String, Object> shipMap)
                throws JsonProcessingException, IllegalXMLException {
            Objects.requireNonNull(shipMap);
            if(!shipMap.containsKey("shipType") || !shipMap.containsKey("sternCoordinate") ||
                    !shipMap.containsKey("direction")) {
                throw new IllegalXMLException("Отсутствует необходимый элемент.");
            }
            return XMLTools.reXML(shipMap, Ship.class);
        }
    }
}
