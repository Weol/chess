package net.rahka.chess.agent.heuristics;

import net.rahka.chess.game.Chess;
import net.rahka.chess.game.Player;

public interface Heuristic {

    int heuristic(Player player, Chess.State state);

}
