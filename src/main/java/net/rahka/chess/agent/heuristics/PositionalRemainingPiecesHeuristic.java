package net.rahka.chess.agent.heuristics;

import net.rahka.chess.configuration.Configurable;
import net.rahka.chess.game.Piece;
import net.rahka.chess.game.Player;
import net.rahka.chess.game.State;

@Configurable(name = "PositionalRemainingHeuristics")
public class PositionalRemainingPiecesHeuristic implements Heuristic {

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
        int blackScore = 0, whiteScore = 0;

        long coveredWhite = state.getThreatenedPositions(Player.WHITE) & state.getWhitePieces();
        long coveredBlack = state.getThreatenedPositions(Player.BLACK) & state.getBlackPieces();

        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                long kernel = (-0x8000000000000000L >>> (63 - (y * 8 + x)));

                if ((kernel & coveredWhite) != 0) {
                    for (Piece piece : Piece.getWhite()) {
                        blackScore += getPieceValue(piece);
                    }
                } else if ((kernel & coveredBlack) != 0) {
                    for (Piece piece : Piece.getBlack()) {
                        blackScore += getPieceValue(piece);
                    }
                }
            }
        }

        for (Piece piece : Piece.values()) {
            if (piece.isWhite()) {
                whiteScore += getPieceValue(piece) * state.getPieceCount(piece);
            } else {
                blackScore += getPieceValue(piece) * state.getPieceCount(piece) * 3;
            }
        }

        return player.isWhite() ? whiteScore - blackScore : blackScore - whiteScore;
    }

}
