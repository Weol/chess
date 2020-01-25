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
public class MiniMaxAgent implements Agent {

    @NonNull @Getter
    private AgentConfiguration configuration;

    @Override
    public Move getMove(Player player, Iterator<Move> moves, State state) {
        List<Move> bestMoves = new ArrayList<>(30);
        int bestValue = Integer.MIN_VALUE;

        while (moves.hasNext()) {
            Move move = moves.next();

            var expanded = state.expand(move);
            int value = min(player, expanded, Integer.MIN_VALUE, Integer.MAX_VALUE, 0);

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

    private int max(Player player, State state, int alpha, int beta, int depth) {
        if (state.isTerminal() || depth >= getConfiguration().getDepthLimit()) return getConfiguration().getHeuristic().heuristic(player, state);

        var moves = state.getAllLegalMoves(player);

        int v = Integer.MIN_VALUE;
        while (moves.hasNext()) {
            Move move = moves.next();

            v = Math.max(v, min(player, state.expand(move), alpha, beta, depth + 1));
            if (v >= beta) return v;
            alpha = Math.max(alpha, v);
        }
        return v;
    }

    private int min(Player player, State state, int alpha, int beta, int depth) {
        if (state.isTerminal() || depth >= getConfiguration().getDepthLimit()) return getConfiguration().getHeuristic().heuristic(player, state);

        var moves = state.getAllLegalMoves(player.not());

        int v = Integer.MAX_VALUE;
        while (moves.hasNext()) {
            Move move = moves.next();

            v = Math.min(v, max(player, state.expand(move), alpha, beta, depth + 1));
            if (v <= alpha) return v;
            beta = Math.min(beta, v);
        }
        return v;
    }
}
