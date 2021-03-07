package net.rahka.chess.agent;

import lombok.RequiredArgsConstructor;
import net.rahka.chess.configuration.Configurable;
import net.rahka.chess.game.Move;
import net.rahka.chess.game.Player;
import net.rahka.chess.game.State;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Always chooses a random move
 */
@Configurable
@RequiredArgsConstructor
public class RandomAgent implements Agent {

	@Override
	public Move getMove(Player player, Collection<Move> moves, State state) {
		List<Move> list = new ArrayList<>(moves);

		int random = (int) (Math.random() * list.size());
		return list.get(random);
	}

}
