package net.rahka.chess.visualizer;

import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;
import lombok.Getter;
import lombok.NonNull;

public class ChessBoard extends Canvas {

	private static final Color WHITE_SQUARE_COLOR = Color.LIGHTGRAY;
	private static final Color BLACK_SQUARE_COLOR = Color.DARKGRAY;

	@Getter
	private final int dimension;

	private boolean[] markedSquares = new boolean[64];

	public ChessBoard(int dimension) {
		this.dimension = dimension;

		widthProperty().addListener((ignored) -> paint());
		heightProperty().addListener((ignored) -> paint());

		paint();
	}

	public void markSquare(int x, int y) {
		markedSquares[y * dimension + x] = true;
		paint();
	}

	public void unmarkSquare(int x, int y) {
		markedSquares[y * dimension + x] = false;
		paint();
	}

	public void unmarkAll() {
		markedSquares = new boolean[64];
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

				if (markedSquares[y * dimension + x]) {
					g.setFill(Color.RED);
					g.fillRect(x * squareSize + 5, y * squareSize + 5, squareSize - 10, squareSize - 10);
				}
			}
		}
	}
}
