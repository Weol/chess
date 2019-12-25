package net.rahka.chess.game;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum Piece {

    WHITE_PAWN(0),
    WHITE_ROOK(1),
    WHITE_KNIGHT(2),
    WHITE_BISHOP(3),
    WHITE_QUEEN(4),
    WHITE_KING(5),
    BLACK_PAWN(6),
    BLACK_ROOK(7),
    BLACK_KNIGHT(8),
    BLACK_BISHOP(9),
    BLACK_QUEEN(10),
    BLACK_KING(11);

    public int index;

    @Getter
    private static Piece[] black = {BLACK_BISHOP, BLACK_KNIGHT, BLACK_ROOK, BLACK_KING, BLACK_PAWN, BLACK_QUEEN};

    @Getter
    private static Piece[] white = {WHITE_PAWN, WHITE_QUEEN, WHITE_BISHOP, WHITE_KNIGHT, WHITE_ROOK, WHITE_KING};

    public static Piece[] of(Player player) {
        if (player.isWhite()) {
            return white;
        } else {
            return black;
        }
    }

    Piece[] adversaries() {
        return (index < 6) ? black : white;
    }



}
