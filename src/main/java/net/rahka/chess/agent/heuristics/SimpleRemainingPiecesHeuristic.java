package net.rahka.chess.agent.heuristics;

import net.rahka.chess.game.Player;
import net.rahka.chess.game.State;

public class SimpleRemainingPiecesHeuristic implements Heuristic {

    @Override
    public int heuristic(Player player, State state) {
        return state.remainingPieces(player) - state.remainingPieces(player.not());
    }

}
