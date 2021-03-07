package net.rahka.chess.game;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.Objects;
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public final class Move {

    @NonNull
    public final Piece piece;

    @Getter
    public final long move;

    @Getter
    public Piece then;

    public Move(Piece piece, int fromX, int fromY, int toX, int toY) {
        this.piece = piece;
        this.move = Board.kernelOf(fromX, fromY) | Board.kernelOf(toX, toY);
    }

}