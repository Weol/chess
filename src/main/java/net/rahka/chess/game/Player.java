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

}
