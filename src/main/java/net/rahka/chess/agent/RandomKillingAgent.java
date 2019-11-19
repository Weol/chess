package net.rahka.chess.agent;

import net.rahka.chess.game.Chess;
import net.rahka.chess.game.Player;
import net.rahka.chess.game.pieces.Move;

import java.util.Collection;
import java.util.Comparator;

/**
 * If it can kill an enemy piece, then it does so, otherwise it makes random moves
 */
public class RandomKillingAgent implements Agent {

	@Override
	public Move getMove(Player player, Collection<Move> moves, Chess.State state) {
		var optional = moves.stream().filter(move -> state.board.getPieceAt(move.toX, move.toY) != null).findAny();

		return optional.orElse(moves.stream().min(Comparator.comparingDouble(move -> Math.random())).orElse(null));
	}

}
