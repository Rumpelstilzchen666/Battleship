package mirea.battleship.Backend;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import mirea.battleship.IllegalXMLException;
import mirea.battleship.XMLTools;

import java.util.Map;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public record BattleSet(int gridSize, ShipType[] shipTypes) {
    public BattleSet {
        if(gridSize < 1) {
            throw new IllegalArgumentException("gridSize(" + gridSize + ") < 1");
        }
        if(shipTypes == null) {
            shipTypes = new ShipType[0];
        }
    }

    public static BattleSet getFromXMLMap(final Map<String, Object> battleSetMap)
            throws IllegalXMLException, JsonProcessingException {
        if(!Objects.requireNonNull(battleSetMap).containsKey("gridSize")) {
            throw new IllegalXMLException("Отсутствуют необходимые параметры.");
        }
        return XMLTools.reXML(battleSetMap, BattleSet.class);
    }
}
