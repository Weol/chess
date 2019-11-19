package net.rahka.chess.agent;

import net.rahka.chess.game.Chess;
import net.rahka.chess.game.Player;
import net.rahka.chess.game.pieces.Move;

import java.util.Collection;
import java.util.Comparator;

/**
 * Always chooses a random move
 */
public class RandomAgent implements Agent {

	@Override
	public Move getMove(Player player, Collection<Move> moves, Chess.State state) {
		return moves.stream().min(Comparator.comparingDouble(move -> Math.random())).orElse(null);
	}

}
