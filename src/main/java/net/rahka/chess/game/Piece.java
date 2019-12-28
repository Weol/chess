package net.rahka.chess.game;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum Piece {

    WHITE_PAWN(0, 4),
    WHITE_ROOK(1, 14),
    WHITE_KNIGHT(2, 8),
    WHITE_BISHOP(3, 14),
    WHITE_QUEEN(4, 28),
    WHITE_KING(5, 8),
    BLACK_PAWN(6, 4),
    BLACK_ROOK(7, 14),
    BLACK_KNIGHT(8, 8),
    BLACK_BISHOP(9, 14),
    BLACK_QUEEN(10, 28),
    BLACK_KING(11, 8);

    public int index;
    public int cacheSize;

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

    public Piece[] adversaries() {
        return (index < 6) ? black : white;
    }

    public boolean isBlack() {
        return index >= 6;
    }

    public boolean isWhite() {
        return index < 6;
    }

    public Player getPlayer() {
        return (isBlack()) ? Player.BLACK : Player.WHITE;
    }

}
