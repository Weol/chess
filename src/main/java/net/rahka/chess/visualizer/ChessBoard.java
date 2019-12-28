package net.rahka.chess.visualizer;

import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;

public class ChessBoard extends Canvas {

	private static final Color WHITE_SQUARE_COLOR = Color.LIGHTGRAY;
	private static final Color BLACK_SQUARE_COLOR = Color.DARKGRAY;

	private Color[] markedSquares = new Color[64];

	public ChessBoard() {
		widthProperty().addListener((ignored) -> paint());
		heightProperty().addListener((ignored) -> paint());

		paint();
	}

	public boolean isSquareHighlighted(int x, int y) {
		return (markedSquares[y * 8 + x] != null);
	}

	public void highlightSquare(int x, int y, Color c) {
		markedSquares[y * 8 + x] = c;
		paint();
	}

	public void unHighlightSquare(int x, int y) {
		markedSquares[y * 8 + x] = null;
		paint();
	}

	public void unHighlightAll() {
		markedSquares = new Color[64];
		paint();
	}

	public void paint() {
		var g = getGraphicsContext2D();

		var squareSize = Math.min(getWidth() / 8, getHeight() / 8);

		for (int x = 0; x < 8; x++) {
			for (int y = 0; y < 8; y++) {
				var squareModulus = x % 2 + y % 2;
				var squareColor = (squareModulus == 0 || squareModulus == 2) ? WHITE_SQUARE_COLOR : BLACK_SQUARE_COLOR;

				g.setFill(squareColor);
				g.fillRect(x * squareSize, y * squareSize, squareSize, squareSize);

				if (markedSquares[y * 8 + x] != null) {
					g.setStroke(markedSquares[y * 8 + x]);
					g.strokeRect(x * squareSize + 4, y * squareSize + 4, squareSize - 8, squareSize - 8);
				}
			}
		}
	}
}
