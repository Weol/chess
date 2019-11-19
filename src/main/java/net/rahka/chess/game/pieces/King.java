package net.rahka.chess.game.pieces;

import lombok.NonNull;
import net.rahka.chess.game.Board;
import net.rahka.chess.game.Player;
import net.rahka.chess.game.Player;

import java.util.ArrayList;
import java.util.Collection;

public class King extends Piece {

	public King(@NonNull Player player, @NonNull int x, @NonNull int y) {
		super(player, x, y);
	}

	private Move createMoveIfValid(Board board, int dx, int dy) {
		if (board.isWithinBounds(x + dx, y + dy)) {
			var piece = board.getPieceAt(x + dx, y + dy);
			if (piece == null || piece.getPlayer() != getPlayer()) {
				return new Move(this, piece, x, y, x + dx, y + dy);
			}
		}
		return null;
	}

	public Collection<Move> getLegalMovements(Board board) {
		var moves = new ArrayList<Move>(8);

		moves.add(createMoveIfValid(board, 1, 1));
		moves.add(createMoveIfValid(board, 0, 1));
		moves.add(createMoveIfValid(board, -1, 1));
		moves.add(createMoveIfValid(board, 1, 0));
		moves.add(createMoveIfValid(board, -1, 0));
		moves.add(createMoveIfValid(board, 1, -1));
		moves.add(createMoveIfValid(board, 0, -1));
		moves.add(createMoveIfValid(board, -1, -1));

		return moves;
	}

}
