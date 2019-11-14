package net.rahka.chess;

import net.rahka.chess.pieces.Piece;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Objects;
import java.util.stream.Collectors;

public class Board implements Iterable<Piece> {

	private HashMap<Integer, Piece> pieces;
	private int dimensions;
	private BoardConfig boardConfig;

	public Board(BoardConfig boardConfig)  {
		dimensions = boardConfig.getDimensions();
		this.boardConfig = boardConfig;
		pieces = new HashMap<>();

		reset();
	}

	public void reset() {
		pieces.clear();

		var pieceConfigs = boardConfig.configurations();
		while (pieceConfigs.hasNext()) {
			var pieceConfig = pieceConfigs.next();
			setPieceAt(pieceConfig.getPiece(), pieceConfig.getStartX(), pieceConfig.getStartY());
		}
	}

	public boolean isWithinBounds(int x, int y) {
		if (x < 0 || x > dimensions - 1) return false;
		if (y < 0 || y > dimensions - 1) return false;

		return true;
	}

	public Piece getPieceAt(int x, int y) {
		if (x < 0 || x > dimensions - 1) throw new IndexOutOfBoundsException("x value is out of bounds (" + x + ", " + y + ")");
		if (y < 0 || y > dimensions - 1) throw new IndexOutOfBoundsException("y value is out of bounds (" + x + ", " + y + ")");

		return pieces.get(Objects.hash(x, y));
	}

	public Piece setPieceAt(Piece piece, int x, int y) {
		if (x < 0 || x > dimensions - 1) throw new IndexOutOfBoundsException("x value is out of bounds (" + x + ", " + y + ")");
		if (y < 0 || y > dimensions - 1) throw new IndexOutOfBoundsException("y value is out of bounds (" + x + ", " + y + ")");

		Piece previousPiece;
		if (piece == null) {
			previousPiece = pieces.remove(Objects.hash(x, y));
		} else {
			previousPiece = pieces.put(Objects.hash(x, y), piece);

			piece.setX(x);
			piece.setY(y);
		}

		return previousPiece;
	}

	public Piece unsetPieceAt(int x, int y) {
		return setPieceAt(null, x, y);
	}

	public Collection<Piece> getAllPieces() {
		return pieces.values();
	}

	public Collection<Piece> getWhitePieces() {
		return pieces.values().stream()
			.filter(piece -> piece.getPlayer() == Player.WHITE)
			.collect(Collectors.toUnmodifiableList());
	}

	public Collection<Piece> getBlackPieces() {
		return pieces.values().stream()
			.filter(piece -> piece.getPlayer() == Player.BLACK)
			.collect(Collectors.toUnmodifiableList());
	}

	@Override
	public Iterator<Piece> iterator() {
		return pieces.values().iterator();
	}

}
