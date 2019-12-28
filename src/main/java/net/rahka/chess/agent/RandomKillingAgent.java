package net.rahka.chess.agent;

import net.rahka.chess.game.Chess;
import net.rahka.chess.game.Move;
import net.rahka.chess.game.Player;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * If it can kill an enemy piece, then it does so, otherwise it makes random moves
 */
public class RandomKillingAgent implements Agent {

	@Override
	public Move getMove(Player player, Iterator<Move> moves, Chess.State state) {
		List<Move> sorted = new ArrayList<>(100);
		int bestValue = Integer.MIN_VALUE;

		while (moves.hasNext()) {
			Move move = moves.next();

			int value = state.remainingPieces(player.not()) - state.expand(move.piece, move.move).remainingPieces(player.not());
			if (value > bestValue) {
				sorted.clear();
				sorted.add(move);
				bestValue = value;
			} else if (value == bestValue) {
				sorted.add(move);
			}
		}

		int random = (int) (Math.random() * sorted.size());
		return sorted.get(random);
	}

}
