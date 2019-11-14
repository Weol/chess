package net.rahka.chess.pieces;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public final class Move implements Cloneable {

	public Move(Move move) {
		piece = move.piece;
		victim = move.victim;
		fromX = move.fromX;
		fromY = move.fromY;
		toX = move.toX;
		toY = move.toY;
	}

	@NonNull
	public Piece piece;

	public Piece victim;

	@NonNull
	public int fromX, fromY, toX, toY;

}
