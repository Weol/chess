package net.rahka.chess.visualizer;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.Group;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.rahka.chess.IO;
import net.rahka.chess.game.Board;
import net.rahka.chess.game.Move;
import net.rahka.chess.game.Piece;

import java.util.Iterator;

public class ChessBoard extends Pane {

	private static final int PIECE_PADDING = 10;

	private static final Color WHITE_SQUARE_COLOR = Color.LIGHTGRAY;
	private static final Color BLACK_SQUARE_COLOR = Color.DARKGRAY;
	private static final Color SELECTED_SQUARE_COLOR = Color.FORESTGREEN;
	private static final Color MOVABLE_SQUARE_COLOR = SELECTED_SQUARE_COLOR;
	private static final Color THREAT_SQUARE_COLOR = Color.ORANGERED;

	private Canvas canvas;
	private ChessPieceView[] imageViews = new ChessPieceView[8*4];

	private Board board;

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

		Group piecesGroup = new Group();
		piecesGroup.setManaged(false);
		piecesGroup.translateXProperty().bind(canvas.layoutXProperty().add(PIECE_PADDING));
		piecesGroup.translateYProperty().bind(canvas.layoutYProperty().add(PIECE_PADDING));

		for (int i = 0; i < imageViews.length; i++) {
			var view = new ChessPieceView();
			view.fitWidthProperty().bind(size.divide(8).subtract(PIECE_PADDING * 2));
			view.fitHeightProperty().bind(view.fitWidthProperty());
			view.xProperty().bind(view.fitWidthProperty().add(PIECE_PADDING * 2).multiply(view.boardXProperty()));
			view.yProperty().bind(view.fitHeightProperty().add(PIECE_PADDING * 2).multiply(view.boardYProperty()));
			view.setMouseTransparent(true);
			view.setSmooth(true);
			piecesGroup.getChildren().add(view);

			imageViews[i] = view;
		}

		getChildren().add(canvas);
		getChildren().add(piecesGroup);

		paint();
	}

	private void onMouseMoved(MouseEvent e) {
		int size = (int) canvas.getWidth() / 8;
		int x = (int) e.getX() / size, y = (int) e.getY() / size;

		x = Math.max(0, Math.min(7, x));
		y = Math.max(0, Math.min(7, y));

		if (hoveredSquare == null || x != hoveredSquare.x || y != hoveredSquare.y) {
			if (hoveredSquare != null) onSquareMouseExit(hoveredSquare);

			hoveredSquare = new Square();
			for (ChessPieceView imageView : imageViews) {
				if (imageView.getBoardX() == x && imageView.getBoardY() == y) {
					hoveredSquare.piece = imageView.getPiece();
					break;
				}
			}

			hoveredSquare.x = x;
			hoveredSquare.y = y;
			onSquareMouseEnter(hoveredSquare);

			paint();
		}
	}

	private void onSquareMouseEnter(Square square) {
		long kernel = Board.kernelOf(square.x, square.y);
		if (selectedSquare != null && selectedSquare.piece != null && (kernel & victims) != 0 && getShowThreats()) {
			Board next = new Board(board);
			next.move(new Move(selectedSquare.piece, Board.kernelOf(selectedSquare.x, selectedSquare.y) | kernel));

			var moves = next.getAllLegalMoves(selectedSquare.piece.getPlayer().not());

			hoverAttackers = 0;
			while (moves.hasNext()) {
				Move move = moves.next();

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
		if (selected == kernel) {
			unselect();
			return;
		}

		ChessPieceView view = null;
		if ((kernel & board.getAllPieces()) != 0) {
			for (ChessPieceView imageView : imageViews) {
				if (imageView.getBoardX() == x && imageView.getBoardY() == y) {
					view = imageView;
					break;
				}
			}
		}

		Piece piece = (view == null) ? null : view.getPiece();
		if (squareMousePressedHandler != null) squareMousePressedHandler.handle(piece, x, y, e);

		if (!e.isPrimaryButtonDown()) return;

		if (view == null) {
			onSquarePressed(x, y);
		} else {
			onPiecePressed(view.getPiece(), x, y, view);
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

	private void onPiecePressed(@NonNull Piece piece, int x, int y, ChessPieceView view) {
		if ((piece.isBlack() && !getCanSelectBlack()) || piece.isWhite() && !getCanSelectWhite()) {
			paint();
			return;
		}

		onSquarePressed(x, y);

		attackers = 0;
		victims = 0;
		threatenedVictims = 0;

		long kernel = Board.kernelOf(x, y);

		selected = kernel;
		selectedSquare = new Square();
		selectedSquare.x = x;
		selectedSquare.y = y;
		selectedSquare.piece = piece;

		if (getShowThreats()) {
			Iterator<Move> moves = board.getAllLegalMoves(piece.getPlayer().not());
			while (moves.hasNext()) {
				Move move = moves.next();

				if ((kernel & move.move) != 0) {
					attackers |= move.move;
				}
			}
			if (attackers != 0) attackers ^= kernel;
		}

		{
			boolean showThreats = getShowThreats();
			Iterator<Move> moves = board.getAllLegalMoves(piece, x, y);
			while (moves.hasNext()) {
				Move move = moves.next();

				if ((kernel & move.move) != 0) {
					victims |= move.move;
				}

				if (showThreats) {
					Board next = new Board(board);
					next.move(move);

					long nextKernel = (move.move ^ kernel);

					var nextMoves = next.getAllLegalMoves(piece.getPlayer().not());
					while (nextMoves.hasNext()) {
						var nextMove = nextMoves.next();

						if ((nextKernel & nextMove.move) != 0) {
							threatenedVictims |= nextMove.move;
						}
					}
				}
			}
			if (victims != 0) victims ^= kernel;
		}

		paint();
	}

	public void setBoardState(long[] state) {
		this.board = new Board(state);

		unselect();

		int index = 0;
		for (Piece piece : Piece.values()) {
			long pieces = state[piece.index];
			for (int i = 0; i < 64; i++) {
				long kernel = (-0x8000000000000000L >>> (63 - i));
				if ((pieces & kernel) != 0) {
					var view = imageViews[index++];
					view.setVisible(true);
					view.setPiece(piece);
					view.setBoardX(i % 8);
					view.setBoardY(i / 8);

					view.setOnMousePressed((ignored) -> onPiecePressed(piece, view.getBoardX(), view.getBoardY(), view));
				}
			}
		}

		for (int i = index; i < imageViews.length; i++) {
			var view = imageViews[index++];
			view.setVisible(false);
		}
	}

	public void paint() {
		var g = canvas.getGraphicsContext2D();

		var squareWidth = canvas.getWidth() / 8;
		var squareHeight = canvas.getHeight() / 8;

		long allPieces = 0;
		if (board != null) allPieces = board.getAllPieces();

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
			}
		}
	}

	private static class ChessPieceView extends ImageView {

		private Image getPieceImage(Piece piece) {
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

		@Getter
		private Piece piece;

		private final IntegerProperty boardXProperty = new SimpleIntegerProperty(0);
		public int getBoardX() {return boardXProperty.get();}
		public void setBoardX(int value) {boardXProperty.set(value);}
		public IntegerProperty boardXProperty() {return boardXProperty;}

		private final IntegerProperty boardYProperty = new SimpleIntegerProperty(0);
		public int getBoardY() {return boardYProperty.get();}
		public void setBoardY(int value) {boardYProperty.set(value);}
		public IntegerProperty boardYProperty() {return boardYProperty;}

		public void setPiece(Piece piece) {
			this.piece = piece;
			setImage(getPieceImage(piece));
			setSmooth(true);
		}

	}

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
