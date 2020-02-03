package net.rahka.chess.game;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.rahka.chess.agent.Agent;

import java.util.ArrayList;
import java.util.function.Consumer;

@RequiredArgsConstructor
public class Match {

	public enum State {
		PREPARED,
		ONGOING,
		INTERRUPTED,
		FINISHED
	}

	@Getter
	ArrayList<long[]> moves = new ArrayList<>(16);

	@NonNull @Getter
	Board board;

	@Getter @Setter
	Agent whiteAgent, blackAgent;

	@Getter
	Player currentPlayer;

	@Getter
	Player winner;

	@Setter
	Consumer<State> onStateChangeHandler;

	@Setter
	Consumer<Player> onCurrentPlayerChangeHandler;

	@Setter
	Consumer<long[]> onBoardStateChangeHandler;

	@Getter
	State state;

	Thread thread;

	public void setCurrentPlayer(Player player) {
		currentPlayer = player;
		if (onCurrentPlayerChangeHandler != null) onCurrentPlayerChangeHandler.accept(player);
	}

	void setMatchState(State matchState) {
		state = matchState;
		if (onStateChangeHandler != null) onStateChangeHandler.accept(matchState);
	}

	public void start() {
		thread.start();
	}

	public void interrupt() {
		thread.interrupt();
		try {
			thread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void join() {
		try {
			thread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
