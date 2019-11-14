package net.rahka.chess;

public enum Player {

	WHITE,
	BLACK;

	static Player not(Player player) {
		return (player == WHITE) ? BLACK : WHITE;
	}

}
