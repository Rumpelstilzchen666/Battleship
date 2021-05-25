package javaCode;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.shape.Polygon;

import java.util.Arrays;

public class GridUI {
    public static GridPane prepareBattleGrid(final GridPane grid, final int size, final int cellSize) {
        return prepareBattleGrid(grid, size, size, cellSize);
    }

    public static GridPane prepareBattleGrid(GridPane grid, final int nRows, final int nCols, final int cellSize) {
        if(grid == null) {
            grid = new GridPane();
        }
        grid.getStyleClass().add("grid");
        grid.setGridLinesVisible(true);
        final double width = cellSize * (nCols + 1), height = cellSize * (nRows + 1);
        grid.setMinSize(width, height);
        grid.setMaxSize(width, height);
        grid.getColumnConstraints().addAll(getColumnConstraintsForGrid(nCols + 1));
        grid.getRowConstraints().addAll(getRowConstraintsForGrid(nRows + 1));

        for(int i = 1; i < nCols + 1; i++) {
            grid.add(getLabelForGrid((char) ('А' + i - 1) + "", cellSize), i, 0);
        }
        for(int i = 1; i < nRows + 1; i++) {
            grid.add(getLabelForGrid(i + "", cellSize), 0, i);
        }

        final GridPane gridBattlefield = new GridPane();
        gridBattlefield.getStyleClass().addAll("grid-battlefield", "grid");
        gridBattlefield.setGridLinesVisible(true);
        gridBattlefield.getColumnConstraints().addAll(getColumnConstraintsForGrid(nCols));
        gridBattlefield.getRowConstraints().addAll(getRowConstraintsForGrid(nRows));
        grid.add(gridBattlefield, 1, 1, nCols, nRows);
        return grid;
    }

    public static ColumnConstraints[] getColumnConstraintsForGrid(final int nCols) {
        ColumnConstraints[] columnConstraints = new ColumnConstraints[nCols];
        for(int colN = 0; colN < nCols; colN++) {
            columnConstraints[colN] = new ColumnConstraints();
            columnConstraints[colN].setPercentWidth(100);
            columnConstraints[colN].setHalignment(HPos.CENTER);
        }
        return columnConstraints;
    }

    public static RowConstraints[] getRowConstraintsForGrid(final int nRows) {
        RowConstraints[] rowConstraints = new RowConstraints[nRows];
        for(int rowN = 0; rowN < nRows; rowN++) {
            rowConstraints[rowN] = new RowConstraints();
            rowConstraints[rowN].setPercentHeight(100);
            rowConstraints[rowN].setValignment(VPos.CENTER);
        }
        return rowConstraints;
    }

    public static Label getLabelForGrid(final String text, final int cellSize) {
        Label label = new Label(text);
        label.getStyleClass().add("grid-label");
        label.setStyle("-fx-font-size: " + cellSize / 3 + ';');
        label.setPadding(new Insets(cellSize * 0.1));
        return label;
    }

    public static Polygon getShip(final int widthInCells, final int cellSize) {
        final double padding = cellSize * 0.1;
        final double shipLen = cellSize * widthInCells - padding * 2;
        final double shipWidth = cellSize - padding * 2;
        final Polygon ship = new Polygon(0, 0, shipLen * 3 / 4, 0, shipLen, shipWidth / 2,
                shipLen * 3 / 4, shipWidth, 0, shipWidth);
        ship.getStyleClass().add("ship");
        return ship;
    }

    public static double getHeight(Polygon polygon) {
        final Double[] pointsCoordinates = polygon.getPoints().toArray(new Double[0]);
        final int nPoints = pointsCoordinates.length / 2;
        final double[] pointsY = new double[nPoints];
        for(int i = 0; i < nPoints; i++) {
            pointsY[i] = pointsCoordinates[2 * i + 1];
        }
        return Arrays.stream(pointsY).max().getAsDouble() - Arrays.stream(pointsY).min().getAsDouble();
    }

    public static double getWidth(Polygon polygon) {
        final Double[] pointsCoordinates = polygon.getPoints().toArray(new Double[0]);
        final int nPoints = pointsCoordinates.length / 2;
        final double[] pointsX = new double[nPoints];
        for(int i = 0; i < nPoints; i++) {
            pointsX[i] = pointsCoordinates[2 * i];
        }
        return Arrays.stream(pointsX).max().getAsDouble() - Arrays.stream(pointsX).min().getAsDouble();
    }
}
