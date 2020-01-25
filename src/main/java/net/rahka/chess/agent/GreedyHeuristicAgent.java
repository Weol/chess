package net.rahka.chess.agent;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.rahka.chess.game.Move;
import net.rahka.chess.game.Player;
import net.rahka.chess.game.State;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@RequiredArgsConstructor
public class GreedyHeuristicAgent implements Agent {

    @NonNull @Getter
    private AgentConfiguration configuration;

    @Override
    public Move getMove(Player player, Iterator<Move> moves, State state) {
        List<Move> bestMoves = new ArrayList<>(10);
        int bestValue = Integer.MIN_VALUE;

        while (moves.hasNext()) {
            Move move = moves.next();

            State expanded = state.expand(move);
            int value = getConfiguration().getHeuristic().heuristic(player, expanded);
            if (value > bestValue) {
                bestMoves.clear();
                bestMoves.add(move);
                bestValue = value;
            } else if (value == bestValue) {
                bestMoves.add(move);
            }
        }

        if (bestMoves.isEmpty()) {
            Agent randomAgent = new RandomAgent(getConfiguration());
            return randomAgent.getMove(player, moves, state);
        }
        return bestMoves.get((int) (Math.random() * bestMoves.size()));
    }

}
