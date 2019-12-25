package net.rahka.chess.agent;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.rahka.chess.game.Chess;
import net.rahka.chess.game.Move;
import net.rahka.chess.game.Piece;
import net.rahka.chess.game.Player;

import java.util.Collection;
import java.util.List;

public interface Agent {

	Move getMove(Player player, Collection<Move> moves, Chess.State state);

}
