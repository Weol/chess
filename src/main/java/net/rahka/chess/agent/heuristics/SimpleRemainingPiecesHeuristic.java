package net.rahka.chess.agent.heuristics;

import lombok.RequiredArgsConstructor;
import net.rahka.chess.configuration.Configurable;
import net.rahka.chess.configuration.ConfigurableBoolean;
import net.rahka.chess.configuration.ConfigurableFloatingPoint;
import net.rahka.chess.configuration.ConfigurableString;
import net.rahka.chess.game.Player;
import net.rahka.chess.game.State;

@Configurable
@RequiredArgsConstructor
public class SimpleRemainingPiecesHeuristic implements Heuristic {

    @ConfigurableString(name = "lmao", def = "Kek 2k")
    final String lmao;

    @ConfigurableFloatingPoint(name = "kek", def = 232.12)
    final double kek;

    @ConfigurableBoolean(name = "ayy", def = true)
    final boolean ayy;

    @Configurable(name = "Asd")
    final Heuristic heuristic;

    @Override
    public int heuristic(Player player, State state) {
        return state.remainingPieces(player) - state.remainingPieces(player.not());
    }

}
