package net.rahka.chess.game;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.Objects;

@RequiredArgsConstructor
public final class Move {

    @NonNull
    public final Piece piece;

    @Getter
    public final long move;

    @Getter
    public Piece then;

}