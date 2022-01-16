package net.rahka.chess.visualizer;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.rahka.chess.IO;
import net.rahka.chess.game.Board;
import net.rahka.chess.game.Move;
import net.rahka.chess.game.Piece;
import net.rahka.chess.game.State;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

public class ChessBoard extends Pane {

	private static final int PIECE_PADDING = 10;

	private static final Color WHITE_SQUARE_COLOR = Color.LIGHTGRAY;
	private static final Color BLACK_SQUARE_COLOR = Color.DARKGRAY;
	private static final Color SELECTED_SQUARE_COLOR = Color.FORESTGREEN;
	private static final Color MOVABLE_SQUARE_COLOR = SELECTED_SQUARE_COLOR;
	private static final Color THREAT_SQUARE_COLOR = Color.ORANGERED;

	private final Canvas canvas;

	private State boardState;

	private Square hoveredSquare, selectedSquare;

	private long selected, attackers, victims, hoverAttackers, threatenedVictims;

	@Getter @Setter
	private PieceMoveHandler pieceMoveHandler;

	@Getter @Setter
	private SquareMousePressedHandler squareMousePressedHandler;

	private final BooleanProperty canSelectWhiteProperty = new SimpleBooleanProperty(true);
	public boolean getCanSelectWhite() {return canSelectWhiteProperty.get();}
	public void setCanSelectWhite(boolean value) {canSelectWhiteProperty.set(value);}
	public BooleanProperty canSelectWhiteProperty() {return canSelectWhiteProperty;}

	private final BooleanProperty canSelectBlackProperty = new SimpleBooleanProperty(true);
	public boolean getCanSelectBlack() {return canSelectBlackProperty.get();}
	public void setCanSelectBlack(boolean value) {canSelectBlackProperty.set(value);}
	public BooleanProperty canSelectBlackProperty() {return canSelectBlackProperty;}

	private final BooleanProperty showThreatsProperty = new SimpleBooleanProperty(true);
	public boolean getShowThreats() {return showThreatsProperty.get();}
	public void setShowThreats(boolean value) {showThreatsProperty.set(value);}
	public BooleanProperty showThreatsProperty() {return showThreatsProperty;}

	public ChessBoard() {
		var size = Bindings.min(heightProperty(), widthProperty());
		size.addListener((ignored) -> paint());

		canvas = new Canvas();
		canvas.widthProperty().bind(size);
		canvas.heightProperty().bind(size);
		canvas.layoutXProperty().bind(widthProperty().divide(2).subtract(size.divide(2)));
		canvas.layoutYProperty().bind(heightProperty().divide(2).subtract(size.divide(2)));
		canvas.setOnMousePressed(this::onMousePressed);
		canvas.setOnMouseMoved(this::onMouseMoved);

		getChildren().add(canvas);

		paint();
	}

	private void onMouseMoved(MouseEvent e) {
		int size = (int) canvas.getWidth() / 8;
		int x = (int) e.getX() / size, y = (int) e.getY() / size;

		x = Math.max(0, Math.min(7, x));
		y = Math.max(0, Math.min(7, y));

		long kernel = Board.kernelOf(x, y);

		if (hoveredSquare == null || x != hoveredSquare.x || y != hoveredSquare.y) {
			if (hoveredSquare != null) onSquareMouseExit(hoveredSquare);

			hoveredSquare = new Square(x, y, getPieceAt(kernel));

			hoveredSquare.x = x;
			hoveredSquare.y = y;
			onSquareMouseEnter(hoveredSquare);

			paint();
		}
	}

	private void onSquareMouseEnter(Square square) {
		long kernel = Board.kernelOf(square.x, square.y);
		if (selectedSquare != null && selectedSquare.piece != null && (kernel & victims) != 0 && getShowThreats()) {
			var nextBoardState = boardState.expand(new Move(selectedSquare.piece, selectedSquare.x, selectedSquare.y, square.x, square.y));

			var moves = nextBoardState.getMoves(selectedSquare.piece.getPlayer().not());

			hoverAttackers = 0;
			for (Move move : moves) {
				if ((kernel & move.move) != 0) {
					hoverAttackers |= move.move;
				}
			}
			if (hoverAttackers != 0) hoverAttackers ^= kernel;
		}
	}

	private void onSquareMouseExit(Square square) {
		hoverAttackers = 0;
	}

	private void onMousePressed(MouseEvent e) {
		int size = (int) canvas.getWidth() / 8;
		int x = (int) e.getX()/ size, y = (int) e.getY() / size;

		x = Math.max(0, Math.min(7, x));
		y = Math.max(0, Math.min(7, y));

		long kernel = Board.kernelOf(x, y);
		if (selected == kernel || (selected != 0 && e.isSecondaryButtonDown())) {
			unselect();
			return;
		}

		Piece piece = getPieceAt(kernel);
		if (squareMousePressedHandler != null) squareMousePressedHandler.handle(piece, x, y, e);

		if (!e.isPrimaryButtonDown()) return;

		if (piece == null) {
			onSquarePressed(x, y);
		} else {
			onPiecePressed(piece, x, y);
			onSquarePressed(x, y);
		}
	}

	private void onSquarePressed(int x, int y) {
		long kernel = Board.kernelOf(x, y);

		if (selected != 0 && victims != 0 && (victims & kernel) != 0 && pieceMoveHandler != null) {
			int fromX = Long.numberOfTrailingZeros(selected) % 8;
			int fromY = Long.numberOfTrailingZeros(selected) / 8;

			pieceMoveHandler.handle(selectedSquare.piece, fromX, fromY, x, y);
		}
	}

	private void unselect() {
		selected = 0;
		attackers = 0;
		victims = 0;
		threatenedVictims = 0;

		selectedSquare = null;

		paint();
	}

	private void onPiecePressed(@NonNull Piece piece, int x, int y) {
		if ((piece.isBlack() && !getCanSelectBlack()) || piece.isWhite() && !getCanSelectWhite()) {
			paint();
			return;
		}

		attackers = 0;
		victims = 0;
		threatenedVictims = 0;

		long kernel = Board.kernelOf(x, y);

		selected = kernel;
		selectedSquare = new Square(x, y, piece);

		long dangerous = 0;
		if (getShowThreats()) {
			if (piece.getPlayer().isWhite()) {
				dangerous = boardState.getDangerousWhitePositions();
			} else {
				dangerous = boardState.getDangerousBlackPositions();
			}
		}

		if (boardState.getMoves(x, y) != null) {
			for (Move move : boardState.getMoves(x, y)) {
				long destination = kernel ^ move.move;
				victims |= destination;

				if (getShowThreats()) {
					if ((destination & dangerous) != 0) {
						threatenedVictims |= destination;
					}
				}
			}
		}

		for (Move move : boardState.getMoves(piece.getPlayer().not())) {
			if ((move.move & kernel) != 0) {
				attackers |= (move.move ^ kernel);
			}
		}

		paint();
	}

	public void setBoardState(long[] boardState) {
		this.boardState = new Board(boardState).getBoardState();
		unselect();
		paint();
	}

	private Piece getPieceAt(long kernel) {
		if (boardState == null) return null;
		for (int i = 0; i < boardState.getBoard().length; i++) {
			if ((kernel & boardState.getBoard()[i]) != 0) {
				return Piece.values()[i];
			}
		}
		return null;
	}

	public void paint() {
		var g = canvas.getGraphicsContext2D();

		var squareWidth = canvas.getWidth() / 8;
		var squareHeight = canvas.getHeight() / 8;

		long allPieces = 0;
		if (boardState != null) allPieces = boardState.getAllPieces();

		long hoveredKernel = 0;
		if (hoveredSquare != null) hoveredKernel = Board.kernelOf(hoveredSquare.x, hoveredSquare.y);

		for (int bx = 0; bx < 8; bx++) {
			for (int by = 0; by < 8; by++) {
				var squareModulus = bx % 2 + by % 2;
				var squareColor = (squareModulus == 0 || squareModulus == 2) ? WHITE_SQUARE_COLOR : BLACK_SQUARE_COLOR;
				double x = bx * squareWidth, y = by * squareHeight;

				long kernel = (-0x8000000000000000L >>> (63 - (by * 8 + bx)));

				g.setFill(squareColor);
				g.fillRect(x, y, squareWidth, squareHeight);

				if ((kernel & selected) != 0) {
					g.setFill(SELECTED_SQUARE_COLOR);
					g.fillRect(x, y, squareWidth, squareHeight);
				} else if ((kernel & attackers) != 0 || (kernel & hoverAttackers) != 0) {
					g.setFill(THREAT_SQUARE_COLOR);
					g.fillRect(x, y, squareWidth, squareHeight);
				}

				if ((kernel & victims) != 0) {
					double width = squareWidth / 4, height = squareHeight / 4;
					if ((kernel & hoveredKernel) != 0) {
						width += 2;
						height += 2;
					}

					if ((kernel & threatenedVictims) != 0) {
						g.setFill(THREAT_SQUARE_COLOR);
					} else {
						g.setFill(MOVABLE_SQUARE_COLOR);
					}

					if ((allPieces & kernel) != 0) {
						g.fillPolygon(new double[] {x, x + width, x }, new double[] {y, y, y + height}, 3);
						g.fillPolygon(new double[] {x + squareWidth - width, x + squareWidth, x + squareWidth}, new double[] {y, y, y + height}, 3);
						g.fillPolygon(new double[] {x, x, x + width}, new double[] {y  + squareHeight - height, y + squareHeight, y + squareHeight}, 3);
						g.fillPolygon(new double[] {x + squareWidth - width, x + squareWidth, x + squareWidth}, new double[] {y + squareHeight, y + squareHeight, y + squareHeight - height}, 3);
					} else {
						g.fillOval(x + squareWidth / 2 - width / 2, y + squareHeight / 2 - height / 2, width, height);
					}
				}

				var piece = getPieceAt(kernel);
				if (piece != null) {
					var img = getPieceImage(piece);

					double pieceSize = squareWidth - PIECE_PADDING * 2;

					g.drawImage(img, x + squareWidth / 2 - pieceSize / 2, y + squareHeight / 2 - pieceSize / 2, pieceSize, pieceSize);
				}
			}
		}
	}

	private static Image getPieceImage(Piece piece) {
		switch (piece) {
			case WHITE_PAWN:
				return IO.image(IO.Images.WHITE_PAWN);
			case WHITE_ROOK:
				return IO.image(IO.Images.WHITE_ROOK);
			case WHITE_KNIGHT:
				return IO.image(IO.Images.WHITE_KNIGHT);
			case WHITE_BISHOP:
				return IO.image(IO.Images.WHITE_BISHOP);
			case WHITE_QUEEN:
				return IO.image(IO.Images.WHITE_QUEEN);
			case WHITE_KING:
				return IO.image(IO.Images.WHITE_KING);
			case BLACK_PAWN:
				return IO.image(IO.Images.BLACK_PAWN);
			case BLACK_ROOK:
				return IO.image(IO.Images.BLACK_ROOK);
			case BLACK_KNIGHT:
				return IO.image(IO.Images.BLACK_KNIGHT);
			case BLACK_BISHOP:
				return IO.image(IO.Images.BLACK_BISHOP);
			case BLACK_QUEEN:
				return IO.image(IO.Images.BLACK_QUEEN);
			case BLACK_KING:
				return IO.image(IO.Images.BLACK_KING);
		}
		return IO.image(IO.Images.BLACK_ROOK);
	}

	@AllArgsConstructor
	private static class Square {

		private int x, y;
		private Piece piece;

	}

	public interface PieceMoveHandler {

		void handle(Piece piece, int fromX, int fromY, int toX, int toY);

	}

	public interface SquareMousePressedHandler {

		void handle(Piece piece, int x, int y, MouseEvent e);

	}

}
