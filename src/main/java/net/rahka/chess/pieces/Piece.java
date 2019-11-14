package net.rahka.chess.pieces;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.PackagePrivate;
import net.rahka.chess.Board;
import net.rahka.chess.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

@RequiredArgsConstructor(onConstructor_ = @PackagePrivate)
public abstract class Piece {

	@NonNull @Getter
	private Player player;

	@NonNull @Getter @Setter
	protected int x, y;

	Collection<Move> scanDirection(Board board, int dx, int dy) {
		if (dx == 0 && dy == 0) throw new IllegalArgumentException("Both dx and dy cannot be 0");

		if (!board.isWithinBounds(x + dx, y + dy)) return new ArrayList<>();

		var moves = new ArrayList<Move>(10);

		Piece piece = null;
		while (board.isWithinBounds(x + dx, y + dy) && piece == null) {
			piece = board.getPieceAt(x + dx, y + dy);

			if (piece == null) {
				moves.add(new Move(this, x, y, x + dx, y + dy));

				if (board.isWithinBounds(x + dx, y + dy)) {
					dx += dx;
					dy += dy;
				}
			}
		}

		if (piece != null && piece.getPlayer() != getPlayer()) {
			moves.add(new Move(this, piece, x, y, x + dx, y + dy));
		}

		return moves;
	}

	public abstract Collection<Move> getLegalMovements(Board board);

	public int getDirection() {
		return (player == Player.WHITE) ? -1 : 1;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Piece piece = (Piece) o;
		return x == piece.x &&
				y == piece.y &&
				player == piece.player;
	}

}
