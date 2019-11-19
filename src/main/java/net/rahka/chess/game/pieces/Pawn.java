package net.rahka.chess.game.pieces;

import lombok.NonNull;
import net.rahka.chess.game.Board;
import net.rahka.chess.game.Player;
import net.rahka.chess.game.Player;

import java.util.ArrayList;
import java.util.Collection;

public class Pawn extends Piece {

	private boolean canDoubleMove = true;

	public Pawn(@NonNull Player player, @NonNull int x, @NonNull int y) {
		super(player, x, y);
	}

	@Override
	public void setX(int x) {
		super.setX(x);
		canDoubleMove = false;
	}

	@Override
	public void setY(int y) {
		super.setY(y);
		canDoubleMove = false;
	}

	boolean isFriendly(Board board, int dx, int dy) {
		return (board.getPieceAt(x + dx, y + dy).getPlayer() == getPlayer());
	}

	boolean isEmpty(Board board, int dx, int dy) {
		return (board.getPieceAt(x + dx, y + dy) == null);
	}

	public Collection<Move> getLegalMovements(Board board) {
		var dir = getDirection();

		var moves = new ArrayList<Move>(4);

		if (board.isWithinBounds(x, y + dir)) {
			if (isEmpty(board, 0, dir)) {
				moves.add(new Move(this, x, y, x, y + dir));
				if (canDoubleMove && isEmpty(board, 0, dir * 2)) {
					moves.add(new Move(this, x, y, x, y + dir * 2));
				}
			}
		}

		if (board.isWithinBounds(x + 1, y + dir) && !isEmpty(board, 1, dir) && !isFriendly(board, 1, dir)) {
			moves.add(new Move(this, board.getPieceAt(x + 1, y + dir), x, y, x + 1, y + dir));
		}

		if (board.isWithinBounds(x - 1, y + dir) && !isEmpty(board, -1, dir) && !isFriendly(board, -1, dir)) {
			moves.add(new Move(this, board.getPieceAt(x - 1, y + dir), x, y, x - 1, y + dir));
		}

		return moves;
	}

}
