package mirea.battleship.Backend;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ShipType(String name, int n, int len) {
    public ShipType {
        if(n < 0) {
            throw new IllegalArgumentException("n(" + n + ") < 0");
        }
        if(len < 1) {
            throw new IllegalArgumentException("length(" + len + ") < 1");
        }
        if(name == null || name.length() == 0) {
            name = len + "-палубный";
        }
    }
}
