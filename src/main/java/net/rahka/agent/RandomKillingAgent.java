package net.rahka.agent;

import net.rahka.chess.Chess;
import net.rahka.chess.pieces.Move;

import java.util.Collection;
import java.util.Comparator;

/**
 * If it can kill an enemy piece, then it does so, otherwise it makes random moves
 */
public class RandomKillingAgent implements Agent {

	@Override
	public Move getMove(Collection<Move> moves, Chess.State state) {
		var optional = moves.stream().filter(move -> state.board.getPieceAt(move.toX, move.toY) != null).findAny();

		return optional.orElse(moves.stream().min(Comparator.comparingDouble(move -> Math.random())).orElse(null));
	}

}
