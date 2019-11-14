package net.rahka.agent;

import net.rahka.chess.Chess;
import net.rahka.chess.pieces.Bishop;
import net.rahka.chess.pieces.King;
import net.rahka.chess.pieces.Knight;
import net.rahka.chess.pieces.Move;
import net.rahka.chess.pieces.Pawn;
import net.rahka.chess.pieces.Piece;
import net.rahka.chess.pieces.Queen;
import net.rahka.chess.pieces.Rook;

import java.util.Collection;
import java.util.Comparator;

/**
 * If it can kill an enemy piece, then it does so, otherwise it makes random moves. If it has more than one option
 * of pieces to kill it will kill the one with the highest value
 */
public class RandomPriorityKillingAgent implements Agent {

	private int getPieceValue(Piece piece) {
		if (piece instanceof Pawn) {
			return 1;
		} else if (piece instanceof Bishop) {
			return 2;
		} else if (piece instanceof King) {
			return 4;
		} else if (piece instanceof Queen) {
			return 3;
		} else if (piece instanceof Knight) {
			return 2;
		} else if (piece instanceof Rook) {
			return 2;
		}
		return 0;
	}

	@Override
	public Move getMove(Collection<Move> moves, Chess.State state) {
		var optional = moves.stream()
			.filter(move -> state.board.getPieceAt(move.toX, move.toY) != null)
			.max(Comparator.comparingInt(move -> getPieceValue(state.board.getPieceAt(move.toX, move.toY))));

		return optional.orElse(moves.stream().min(Comparator.comparingDouble(move -> Math.random())).orElse(null));
	}

}
