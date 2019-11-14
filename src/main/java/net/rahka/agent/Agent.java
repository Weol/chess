package net.rahka.agent;

import net.rahka.chess.Chess;
import net.rahka.chess.pieces.Move;

import java.util.Collection;

public interface Agent {

	Move getMove(Collection<Move> moves, Chess.State state);

}
