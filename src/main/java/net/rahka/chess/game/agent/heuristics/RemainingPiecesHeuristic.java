package net.rahka.chess.game.agent.heuristics;

import lombok.RequiredArgsConstructor;
import net.rahka.chess.configuration.Configurable;
import net.rahka.chess.configuration.ConfigurableFloatingPoint;
import net.rahka.chess.configuration.ConfigurableInt;
import net.rahka.chess.game.Piece;
import net.rahka.chess.game.Player;
import net.rahka.chess.game.State;

@Configurable(name = "Remaining")
@RequiredArgsConstructor
public class RemainingPiecesHeuristic implements Heuristic {

    private int getPieceValue(Piece piece) {
        switch (piece) {
            case WHITE_PAWN:
            case BLACK_PAWN:
                return 1;
            case WHITE_ROOK:
            case BLACK_ROOK:
                return 5;
            case BLACK_KNIGHT:
            case BLACK_BISHOP:
            case WHITE_KNIGHT:
            case WHITE_BISHOP:
                return 3;
            case WHITE_QUEEN:
            case BLACK_QUEEN:
                return 9;
            case WHITE_KING:
            case BLACK_KING:
                return 100000;
        }
        return 0;
    }

    @ConfigurableInt(name = "asd")
    final int asd;

    @Configurable(name = "fallback")
    final Heuristic heuristic;

    @Override
    public int heuristic(Player player, State state) {
        int alliedSum = 0, enemySum = 0;
        for (Piece piece : Piece.of(player)) {
            alliedSum += getPieceValue(piece) * state.getPieceCount(piece);
        }

        for (Piece piece : Piece.of(player.not())) {
            enemySum += getPieceValue(piece) * state.getPieceCount(piece);
        }

        return alliedSum - enemySum;
    }

}
