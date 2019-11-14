package net.rahka.agent;

import net.rahka.chess.Chess;
import net.rahka.chess.pieces.Move;

import java.util.Collection;
import java.util.Comparator;

/**
 * Always chooses a random move
 */
public class RandomAgent implements Agent {

	@Override
	public Move getMove(Collection<Move> moves, Chess.State state) {
		return moves.stream().min(Comparator.comparingDouble(move -> Math.random())).orElse(null);
	}

}
