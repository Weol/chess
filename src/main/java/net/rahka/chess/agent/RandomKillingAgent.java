package net.rahka.chess.agent;

import net.rahka.chess.game.Chess;
import net.rahka.chess.game.Move;
import net.rahka.chess.game.Piece;
import net.rahka.chess.game.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * If it can kill an enemy piece, then it does so, otherwise it makes random moves
 */
public class RandomKillingAgent implements Agent {

	@Override
	public Move getMove(Player player, Collection<Move> moves, Chess.State state) {
		List<Move> sorted = new ArrayList<>(100);

		for (Move move : moves) {
			if (state.remainingPieces(player.not()) > state.expand(move.piece, move.move).remainingPieces(player.not())) {
				sorted.add(move);
			}
		}

		if (sorted.isEmpty()) {
			RandomAgent randomAgent = new RandomAgent();
			return randomAgent.getMove(player, moves, state);
		} else {
			return sorted.get((int) (Math.random() * sorted.size()));
		}
	}

}
