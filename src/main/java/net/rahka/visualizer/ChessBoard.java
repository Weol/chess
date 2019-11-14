package net.rahka.visualizer;

import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;
import lombok.Getter;
import lombok.NonNull;

public class ChessBoard extends Canvas {

	private static final Color WHITE_SQUARE_COLOR = Color.LIGHTGRAY;
	private static final Color BLACK_SQUARE_COLOR = Color.DARKGRAY;

	@NonNull @Getter
	private int dimension;

	public ChessBoard(@NonNull int dimension) {
		this.dimension = dimension;

		widthProperty().addListener((ignored) -> paint());
		heightProperty().addListener((ignored) -> paint());

		paint();
	}

	public void paint() {
		var g = getGraphicsContext2D();

		var squareSize = Math.min(getWidth() / 8, getHeight() / 8);

		for (int x = 0; x < dimension; x++) {
			for (int y = 0; y < dimension; y++) {
				var squareModulus = x % 2 + y % 2;
				var squareColor = (squareModulus == 0 || squareModulus == 2) ? WHITE_SQUARE_COLOR : BLACK_SQUARE_COLOR;

				g.setFill(squareColor);
				g.fillRect(x * squareSize, y * squareSize, squareSize, squareSize);
			}
		}
	}

}
