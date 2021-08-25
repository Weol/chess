package net.rahka.chess.game;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.rahka.chess.agent.Agent;

import java.util.function.Consumer;

public class Match {

	public enum State {
		PREPARED,
		ONGOING,
		INTERRUPTED,
		FINISHED
	}

	@Getter
	final long[] initialState;

	@Getter
	private final Board board;

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

	@Getter
	State state;

	Thread thread;

	public Match(long[] initialState) {
		this.initialState = initialState;
		this.board = new Board(initialState);

		state = State.PREPARED;
	}

	public Match() {
		this(createDefaultBoard());
	}

	private void playMatch(Agent whiteAgent, Agent blackAgent) {
		Board board = getBoard();

		boolean isDraw = false;
		Player winner = null;
		do {
			{
				setCurrentPlayer(Player.WHITE);
				var boardState = board.getBoardState();

				var move = whiteAgent.getMove(Player.WHITE, boardState.getWhiteMoves(), boardState);

				if (move == null) break;

				//int fromI = Long.numberOfTrailingZeros(boardState.getBoard(move.piece) & move.move);
				//int toI = Long.numberOfTrailingZeros((boardState.getBoard(move.piece) & move.move) ^ move.move);

				//System.out.printf("White moved %s from (%d, %d) to (%d, %d)\n", move.piece, fromI % 8, fromI / 8, toI % 8, toI / 8);

				board.move(move);

				if (board.state[Piece.BLACK_KING.index] == 0) {
					winner = Player.WHITE;
					break;
				}
			}

			if (!Thread.currentThread().isInterrupted()) {
				setCurrentPlayer(Player.BLACK);

				var boardState = board.getBoardState();
				var move = blackAgent.getMove(Player.BLACK, boardState.getBlackMoves(), boardState);

				if (move == null) break;

				//int fromI = Long.numberOfTrailingZeros(boardState.getBoard(move.piece) & move.move);
				//int toI = Long.numberOfTrailingZeros((boardState.getBoard(move.piece) & move.move) ^ move.move);

				//System.out.printf("Black moved %s from (%d, %d) to (%d, %d)\n", move.piece, fromI % 8, fromI / 8, toI % 8, toI / 8);

				board.move(move);

				if (board.state[Piece.WHITE_KING.index] == 0) {
					winner = Player.BLACK;
					break;
				}
			}

			if (board.getAllPieces() == (board.state[Piece.WHITE_KING.index] | board.state[Piece.BLACK_KING.index])) {
				isDraw = true;
				break;
			}
		} while (!Thread.currentThread().isInterrupted());

		synchronized (this) {
			if (winner != null || isDraw) {
				this.winner = winner;
				setMatchState(Match.State.FINISHED);
			} else {
				setMatchState(State.INTERRUPTED);
			}
		}
	}

	public synchronized void setCurrentPlayer(Player player) {
		currentPlayer = player;
		if (onCurrentPlayerChangeHandler != null) onCurrentPlayerChangeHandler.accept(player);
	}

	private synchronized void setMatchState(State matchState) {
		state = matchState;
		if (onStateChangeHandler != null) onStateChangeHandler.accept(matchState);
	}

	public void start() {
		if (whiteAgent == null || blackAgent == null) throw new NullPointerException();

		setMatchState(Match.State.ONGOING);
		thread = new Thread(() -> playMatch(whiteAgent, blackAgent));
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

	private static long[] createDefaultBoard() {
		return new long[] {
				0b00000000_00000000_00000000_00000000_00000000_00000000_11111111_00000000L,
				0b00000000_00000000_00000000_00000000_00000000_00000000_00000000_10000001L,
				0b00000000_00000000_00000000_00000000_00000000_00000000_00000000_01000010L,
				0b00000000_00000000_00000000_00000000_00000000_00000000_00000000_00100100L,
				0b00000000_00000000_00000000_00000000_00000000_00000000_00000000_00010000L,
				0b00000000_00000000_00000000_00000000_00000000_00000000_00000000_00001000L,
				0b00000000_11111111_00000000_00000000_00000000_00000000_00000000_00000000L,
				0b10000001_00000000_00000000_00000000_00000000_00000000_00000000_00000000L,
				0b01000010_00000000_00000000_00000000_00000000_00000000_00000000_00000000L,
				0b00100100_00000000_00000000_00000000_00000000_00000000_00000000_00000000L,
				0b00001000_00000000_00000000_00000000_00000000_00000000_00000000_00000000L,
				0b00010000_00000000_00000000_00000000_00000000_00000000_00000000_00000000L
		};
	}

}
