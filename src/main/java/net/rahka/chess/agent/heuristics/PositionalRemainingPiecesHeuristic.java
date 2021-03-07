package net.rahka.chess.agent.heuristics;

import net.rahka.chess.configuration.Configurable;
import net.rahka.chess.game.Player;
import net.rahka.chess.game.State;

@Configurable(name = "PositionalRemainingHeuristics")
public class PositionalRemainingPiecesHeuristic implements Heuristic {

    @Override
    public int heuristic(Player player, State state) {
        return 0;
    }

}
