package net.rahka.chess.agent;

import net.rahka.chess.game.Chess;
import net.rahka.chess.game.Move;
import net.rahka.chess.game.Player;

import java.util.Collection;
import java.util.List;

/**
 * Always chooses a random move
 */
public class RandomAgent implements Agent {

	@Override
	public Move getMove(Player player, Collection<Move> moves, Chess.State state) {
		int random = (int) (Math.random() * moves.size());
		return moves.toArray(new Move[moves.size()])[random];
	}

}
