package net.rahka.chess.game;

public interface StateChangeHandler {

    void onBoardStateChange(long[] state);

}
