package net.rahka.chess.agent;

import net.rahka.chess.game.Chess;
import net.rahka.chess.game.Player;
import net.rahka.chess.game.pieces.Move;

import java.util.Collection;

public interface Agent {

	Move getMove(Player player, Collection<Move> moves, Chess.State state);

}
