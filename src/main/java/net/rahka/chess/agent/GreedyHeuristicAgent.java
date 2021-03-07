package net.rahka.chess.agent;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.rahka.chess.agent.heuristics.Heuristic;
import net.rahka.chess.agent.heuristics.RemainingPiecesHeuristic;
import net.rahka.chess.configuration.Configurable;
import net.rahka.chess.game.Move;
import net.rahka.chess.game.Player;
import net.rahka.chess.game.State;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Configurable
@RequiredArgsConstructor
public class GreedyHeuristicAgent implements Agent {

    @Getter @Configurable(name = "Heuristic", def = RemainingPiecesHeuristic.class)
    final Heuristic heuristic;

    @Getter
    final RandomAgent randomAgent;

    @Override
    public Move getMove(Player player, Collection<Move> moves, State state) {
        List<Move> bestMoves = new ArrayList<>(10);
        int bestValue = Integer.MIN_VALUE;

        for (Move move : moves) {
            State expanded = state.expand(move);
            int value = getHeuristic().heuristic(player, expanded);
            if (value > bestValue) {
                bestMoves.clear();
                bestMoves.add(move);
                bestValue = value;
            } else if (value == bestValue) {
                bestMoves.add(move);
            }
        }

        if (bestMoves.isEmpty()) {
            return randomAgent.getMove(player, moves, state);
        }
        return bestMoves.get((int) (Math.random() * bestMoves.size()));
    }

}
