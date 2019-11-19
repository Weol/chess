package net.rahka.chess.agent;

import net.rahka.chess.agent.heuristics.Heuristic;
import net.rahka.chess.agent.heuristics.RemainingPiecesHeuristic;
import net.rahka.chess.game.Chess;
import net.rahka.chess.game.Player;
import net.rahka.chess.game.pieces.Move;

import java.util.Collection;
import java.util.Comparator;

public class MiniMaxAgent implements Agent {

    private static int DEPTH_LIMIT = 4;

    Heuristic heuristic = new RemainingPiecesHeuristic();

    @Override
    public Move getMove(Player player, Collection<Move> moves, Chess.State state) {
        return moves.stream()
                .max(Comparator.comparingInt(move -> min(player.not(), state.expand(move), Integer.MIN_VALUE, Integer.MAX_VALUE, 0))).orElse(null);
    }

    private int max(Player player, Chess.State state, int alpha, int beta, int depth) {
        if (state.terminal || depth >= DEPTH_LIMIT) return heuristic.heuristic(player, state);

        var moves = state.getAvailableMoves(player);

        int v = Integer.MIN_VALUE;
        for (Move move : moves) {
            v = Math.max(v, min(player.not(), state.expand(move), alpha, beta, depth + 1));
            if (v > beta) return v;
            alpha = Math.max(alpha, v);
        }
        return v;
    }

    private int min(Player player, Chess.State state, int alpha, int beta, int depth) {
        if (state.terminal || depth >= DEPTH_LIMIT) return heuristic.heuristic(player, state);

        var moves = state.getAvailableMoves(player);

        int v = Integer.MAX_VALUE;
        for (Move move : moves) {
            v = Math.min(v, max(player.not(), state.expand(move), alpha, beta, depth + 1));
            if (v > alpha) return v;
            beta = Math.min(beta, v);
        }
        return v;
    }

}
