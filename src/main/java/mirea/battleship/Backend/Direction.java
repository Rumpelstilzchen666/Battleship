package mirea.battleship.Backend;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public enum Direction {
    UP, RIGHT, DOWN, LEFT
}
