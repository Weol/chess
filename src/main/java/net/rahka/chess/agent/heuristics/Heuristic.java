package net.rahka.chess.agent.heuristics;

import net.rahka.chess.game.Player;
import net.rahka.chess.game.State;

public interface Heuristic {

    int heuristic(Player player, State state);

}
