package mirea.battleship.Backend;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Coordinate(int col, int row) { }
