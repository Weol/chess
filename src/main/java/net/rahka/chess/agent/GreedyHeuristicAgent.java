package net.rahka.chess.agent;

import net.rahka.chess.agent.heuristics.Heuristic;
import net.rahka.chess.agent.heuristics.PriorityRemainingPiecesHeuristic;
import net.rahka.chess.game.Chess;
import net.rahka.chess.game.Move;
import net.rahka.chess.game.Player;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GreedyHeuristicAgent implements Agent {

    Heuristic heuristic = new PriorityRemainingPiecesHeuristic();

    @Override
    public Move getMove(Player player, Iterator<Move> moves, Chess.State state) {
        List<Move> bestMoves = new ArrayList<>(10);
        int bestValue = Integer.MIN_VALUE;

        while (moves.hasNext()) {
            Move move = moves.next();

            Chess.State expanded = state.expand(move.piece, move.move);
            int value = heuristic.heuristic(player, expanded);
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

}
