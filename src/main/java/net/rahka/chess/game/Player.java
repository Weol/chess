package net.rahka.chess.game;

public enum Player {

	WHITE,
	BLACK;

	public Player not() {
		return not(this);
	}

	static Player not(Player player) {
		return (player == WHITE) ? BLACK : WHITE;
	}

	public boolean isBlack() {
		return (this == BLACK);
	}

	public boolean isWhite() {
		return (this == WHITE);
	}

}
