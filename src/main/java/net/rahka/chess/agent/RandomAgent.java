package net.rahka.chess.agent;

import net.rahka.chess.game.Chess;
import net.rahka.chess.game.Move;
import net.rahka.chess.game.Player;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Always chooses a random move
 */
public class RandomAgent implements Agent {

	@Override
	public Move getMove(Player player, Iterator<Move> moves, Chess.State state) {
		List<Move> list = new ArrayList<>(100);
		moves.forEachRemaining(list::add);

		int random = (int) (Math.random() * list.size());
		return list.get(random);
	}

}
