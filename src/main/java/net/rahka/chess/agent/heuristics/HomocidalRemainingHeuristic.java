package net.rahka.chess.agent.heuristics;

import net.rahka.chess.configuration.Configurable;
import net.rahka.chess.game.Piece;
import net.rahka.chess.game.Player;
import net.rahka.chess.game.State;

@Configurable
public class HomocidalRemainingHeuristic implements Heuristic {

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

    @Override
    public int heuristic(Player player, State state) {
        int alliedSum = 0, enemySum = 0;
        for (Piece piece : Piece.of(player)) {
            alliedSum += getPieceValue(piece) * state.remainingPieces(piece);
        }

        for (Piece piece : Piece.of(player.not())) {
            enemySum += (getPieceValue(piece) + 2) * state.remainingPieces(piece);
        }

        return alliedSum - enemySum;
    }

}
