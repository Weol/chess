package net.rahka.chess.agent;

import lombok.AllArgsConstructor;
import net.rahka.chess.agent.heuristics.Heuristic;
import net.rahka.chess.agent.heuristics.RemainingPiecesHeuristic;
import net.rahka.chess.game.Chess;
import net.rahka.chess.game.Player;
import net.rahka.chess.game.pieces.Move;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;

public class SimpleHeuristicAgent implements Agent {

    Heuristic heuristic = new RemainingPiecesHeuristic();

    @Override
    public Move getMove(Player player, Collection<Move> moves, Chess.State state) {
        var optional = moves.stream()
                .filter(move -> state.board.getPieceAt(move.toX, move.toY) != null)
                .max(Comparator.comparingInt(move -> heuristic.heuristic(player, state)));

        return optional.orElse(moves.stream().min(Comparator.comparingDouble(move -> Math.random())).orElse(null));
    }

}
