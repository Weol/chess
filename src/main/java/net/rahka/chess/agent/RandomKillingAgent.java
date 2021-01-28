package net.rahka.chess.agent;

import lombok.RequiredArgsConstructor;
import net.rahka.chess.configuration.Configurable;
import net.rahka.chess.game.Move;
import net.rahka.chess.game.Player;
import net.rahka.chess.game.State;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * If it can kill an enemy piece, then it does so, otherwise it makes random moves
 */
@Configurable
@RequiredArgsConstructor
public class RandomKillingAgent implements Agent {

	@Override
	public Move getMove(Player player, Iterator<Move> moves, State state) {
		List<Move> sorted = new ArrayList<>(100);
		int bestValue = Integer.MIN_VALUE;

		while (moves.hasNext()) {
			Move move = moves.next();

			int value = state.remainingPieces(player.not()) - state.expand(move).remainingPieces(player.not());
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
