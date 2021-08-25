package net.rahka.chess.game;

import lombok.*;

import java.util.Objects;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public final class Move {

    @NonNull
    public final Piece piece;

    @Getter
    public final long move;

    @Getter @Setter
    public Piece spawn;

    public Move(Piece piece, int fromX, int fromY, int toX, int toY) {
        this.piece = piece;
        this.move = Board.kernelOf(fromX, fromY) | Board.kernelOf(toX, toY);
    }

}