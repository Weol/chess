package net.rahka.chess.agent;

import net.rahka.chess.game.Move;
import net.rahka.chess.game.Player;
import net.rahka.chess.game.State;

import java.util.Iterator;

public interface Agent {

	Move getMove(Player player, Iterator<Move> moves, State state);

	default void postMove(Move move) {}

}
