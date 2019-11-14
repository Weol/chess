package net.rahka.chess.pieces;

import lombok.NonNull;
import net.rahka.chess.Board;
import net.rahka.chess.Player;

import java.util.ArrayList;
import java.util.Collection;

public class Queen extends Piece {

	public Queen(@NonNull Player player, @NonNull int x, @NonNull int y) {
		super(player, x, y);
	}

	public Collection<Move> getLegalMovements(Board board) {
		var moves = new ArrayList<Move>(32);

		moves.addAll(scanDirection(board, 1, 1));
		moves.addAll(scanDirection(board, -1, 1));
		moves.addAll(scanDirection(board, 1, -1));
		moves.addAll(scanDirection(board, -1, -1));
		moves.addAll(scanDirection(board, 1, 0));
		moves.addAll(scanDirection(board, -1, 0));
		moves.addAll(scanDirection(board, 0, -1));
		moves.addAll(scanDirection(board, 0, 1));

		return moves;
	}

}
