package net.rahka.chess.game.pieces;

import lombok.NonNull;
import net.rahka.chess.game.Board;
import net.rahka.chess.game.Player;

import java.util.ArrayList;
import java.util.Collection;

public class Rook extends Piece {

	public Rook(@NonNull Player player, @NonNull int x, @NonNull int y) {
		super(player, x, y);
	}

	public Collection<Move> getLegalMovements(Board board) {
		var moves = new ArrayList<Move>(16);

		moves.addAll(scanDirection(board, 1, 0));
		moves.addAll(scanDirection(board, -1, 0));
		moves.addAll(scanDirection(board, 0, -1));
		moves.addAll(scanDirection(board, 0, 1));

		return moves;
	}

}
