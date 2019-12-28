package net.rahka.chess.agent;

import net.rahka.chess.game.Chess;
import net.rahka.chess.game.Move;
import net.rahka.chess.game.Player;

import java.util.Iterator;

public interface Agent {

	Move getMove(Player player, Iterator<Move> moves, Chess.State state);

}
