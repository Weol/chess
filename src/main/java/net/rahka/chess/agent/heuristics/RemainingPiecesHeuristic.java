package net.rahka.chess.agent.heuristics;

import net.rahka.chess.game.Chess;
import net.rahka.chess.game.Player;

public class RemainingPiecesHeuristic implements Heuristic {

    @Override
    public int heuristic(Player player, Chess.State state) {
        return state.remainingPieces(player) - state.remainingPieces(player.not());
    }

}
