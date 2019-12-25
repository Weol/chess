package net.rahka.chess.game;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class Move {

    @NonNull
    public final Piece piece;

    public final long move;

}