package net.rahka.chess.game.agent.heuristics;

import net.rahka.chess.game.Player;
import net.rahka.chess.game.State;

public interface Heuristic {

    int heuristic(Player player, State state);

}
