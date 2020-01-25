package net.rahka.chess.agent;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.rahka.chess.game.Move;
import net.rahka.chess.game.Player;
import net.rahka.chess.game.State;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Always chooses a random move
 */
@RequiredArgsConstructor
public class RandomAgent implements Agent {

	@NonNull
	@Getter
	private AgentConfiguration configuration;

	@Override
	public Move getMove(Player player, Iterator<Move> moves, State state) {
		List<Move> list = new ArrayList<>(100);
		moves.forEachRemaining(list::add);

		int random = (int) (Math.random() * list.size());
		return list.get(random);
	}

}
