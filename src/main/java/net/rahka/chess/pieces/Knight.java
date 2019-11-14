package net.rahka.chess.pieces;

import lombok.NonNull;
import net.rahka.chess.Board;
import net.rahka.chess.Player;

import java.util.ArrayList;
import java.util.Collection;

public class Knight extends Piece {

	public Knight(@NonNull Player player, @NonNull int x, @NonNull int y) {
		super(player, x, y);
	}

	private Move createMoveIfValid(Board board, int dx, int dy) {
		if (board.isWithinBounds(getX() + dx, getY() + dy)) {
			var piece = board.getPieceAt(getX() + dx, getY() + dy);
			if (piece == null || piece.getPlayer() != getPlayer()) {
				return new Move(this, piece, getX(), getY(), getX() + dx, getY() + dy);
			}
		}
		return null;
	}

	public Collection<Move> getLegalMovements(Board board) {
		var moves = new ArrayList<Move>(8);

		moves.add(createMoveIfValid(board, 1, 2));
		moves.add(createMoveIfValid(board, 2, 1));
		moves.add(createMoveIfValid(board, -1, 2));
		moves.add(createMoveIfValid(board, -2, 1));
		moves.add(createMoveIfValid(board, 1, -2));
		moves.add(createMoveIfValid(board, 2, -1));
		moves.add(createMoveIfValid(board, -1, -2));
		moves.add(createMoveIfValid(board, -2, -1));

		return moves;
	}

}
