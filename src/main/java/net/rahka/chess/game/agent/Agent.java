package net.rahka.chess.game.agent;

import net.rahka.chess.game.Move;
import net.rahka.chess.game.Player;
import net.rahka.chess.game.State;

import java.util.Collection;

public interface Agent {

	Move getMove(Player player, Collection<Move> moves, State state);

}
