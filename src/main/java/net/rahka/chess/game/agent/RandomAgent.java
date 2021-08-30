package net.rahka.chess.game.agent;

import lombok.RequiredArgsConstructor;
import net.rahka.chess.configuration.Configurable;
import net.rahka.chess.game.Move;
import net.rahka.chess.game.Player;
import net.rahka.chess.game.State;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

/**
 * Always chooses a random move
 */
@Configurable(name = "Random")
@RequiredArgsConstructor
public class RandomAgent implements Agent {

	@Configurable(name = "Random")
	private final Random random;

	@Override
	public Move getMove(Player player, Collection<Move> moves, State state) {
		List<Move> list = new ArrayList<>(moves);

		int index = (int) (random.nextDouble() * list.size());
		return list.get(index);
	}

}
