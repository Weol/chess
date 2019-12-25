package net.rahka.chess.agent;

import net.rahka.chess.agent.heuristics.Heuristic;
import net.rahka.chess.agent.heuristics.PriorityRemainingPiecesHeuristic;
import net.rahka.chess.game.Chess;
import net.rahka.chess.game.Move;
import net.rahka.chess.game.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MiniMaxAgent implements Agent {

    private static int DEPTH_LIMIT = 4;

    Heuristic heuristic = new PriorityRemainingPiecesHeuristic();

    @Override
    public Move getMove(Player player, Collection<Move> moves, Chess.State state) {
        List<Move> bestMoves = new ArrayList<>(30);
        int bestValue = Integer.MIN_VALUE;

        for (Move move : moves) {
            var expanded = state.expand(move.piece, move.move);
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
            Agent randomAgent = new RandomAgent();
            return randomAgent.getMove(player, moves, state);
        }
        return bestMoves.get((int) (Math.random() * bestMoves.size()));
    }

    private int max(Player player, Chess.State state, int alpha, int beta, int depth) {
        if (state.isTerminal() || depth >= DEPTH_LIMIT) return heuristic.heuristic(player, state);

        var moves = state.getAvailableMoves(player);

        int v = Integer.MIN_VALUE;
        for (Move move : moves) {
            v = Math.max(v, min(player, state.expand(move.piece, move.move), alpha, beta, depth + 1));
            if (v >= beta) return v;
            alpha = Math.max(alpha, v);
        }
        return v;
    }

    private int min(Player player, Chess.State state, int alpha, int beta, int depth) {
        if (state.isTerminal() || depth >= DEPTH_LIMIT) return heuristic.heuristic(player, state);

        var moves = state.getAvailableMoves(player.not());

        int v = Integer.MAX_VALUE;
        for (Move move : moves) {
            v = Math.min(v, max(player, state.expand(move.piece, move.move), alpha, beta, depth + 1));
            if (v <= alpha) return v;
            beta = Math.min(beta, v);
        }
        return v;
    }
}
