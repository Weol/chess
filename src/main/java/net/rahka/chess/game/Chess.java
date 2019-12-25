package net.rahka.chess.game;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.rahka.chess.agent.Agent;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Chess {

	@Getter @Setter
	private Agent blackAgent, whiteAgent;

	@Getter
	private Board board;

	private ExecutorService handlerExecutor = Executors.newSingleThreadScheduledExecutor();

	@Setter @Getter
	private BoardStateChangeHandler boardChangeHandler;

	public Chess(Agent whiteAgent, Agent blackAgent) {
		board = new Board();

		this.blackAgent = whiteAgent;
		this.whiteAgent = blackAgent;
	}

	public Player start() {
		board.reset();

		Player winner = null;
		while (winner == null && !Thread.currentThread().isInterrupted()) {
			{
				var move = whiteAgent.getMove(Player.WHITE, board.getAllLegalMoves(Player.WHITE), new State(board.getState()));

				int fromI = Long.numberOfTrailingZeros(board.getPieces(move.piece) & move.move);
				int toI = Long.numberOfTrailingZeros((board.getPieces(move.piece) & move.move) ^ move.move);

				board.move(move.piece, move.move);

				System.out.printf("White moved %s from (%d, %d) to (%d, %d)\n", move.piece, fromI % 8, fromI / 8, toI % 8, toI / 8);

				runBoardChangeHandler();
				if (board.getPieces(Piece.BLACK_KING) == 0) {
					winner = Player.WHITE;
				}
			}

			if (winner == null) {
				var move = blackAgent.getMove(Player.BLACK, board.getAllLegalMoves(Player.BLACK), new State(board.getState()));

				int fromI = Long.numberOfLeadingZeros(board.getPieces(move.piece) & move.move);
				int toI = Long.numberOfLeadingZeros((board.getPieces(move.piece) & move.move) ^ move.move);

				System.out.printf("Black moved %s from (%d, %d) to (%d, %d)\n", move.piece, fromI % 8, fromI / 8, toI % 8, toI / 8);

				board.move(move.piece, move.move);

				runBoardChangeHandler();
				if (board.getPieces(Piece.WHITE_KING) == 0) {
					winner = Player.BLACK;
				}
			}
		}
		return winner;
	}

	private void runBoardChangeHandler() {
		long[] state = new long[12];
		for (Piece piece : Piece.values()) {
			state[piece.index] = board.getPieces(piece);
		}

		if (boardChangeHandler != null) handlerExecutor.submit(() -> {
			boardChangeHandler.onBoardStateChange(state);
		});
	}

	public static class State {

		private final int[] remainingPieces;

		@Getter(lazy = true)
		private final int remainingWhitePieces = calculateRemainingWhitePieces();

		@Getter(lazy = true)
		private final int remainingBlackPieces = calculateRemainingBlackPieces();

		@Getter(lazy = true)
		private final long allBlackPieces = calculateAllBlackPieces();

		@Getter(lazy = true)
		private final long allWhitePieces = calculateAllWhitePieces();

		@Getter(lazy = true)
		private final Board board = createBoard();

		@Getter
		private final long[] state;

		public State(long[] state, int[] remainingPieces) {
			this.state = state;
			this.remainingPieces = remainingPieces;
		}

		public State(long[] state) {
			this(state, new int[] {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1});
		}

		public State expand(Piece piece, long move) {
			long[] newState = new long[] {
					state[0],
					state[1],
					state[2],
					state[3],
					state[4],
					state[5],
					state[6],
					state[7],
					state[8],
					state[9],
					state[10],
					state[11],
			};

			int[] newPieceCounts = new int[] {
					remainingPieces[0],
					remainingPieces[1],
					remainingPieces[2],
					remainingPieces[3],
					remainingPieces[4],
					remainingPieces[5],
					remainingPieces[6],
					remainingPieces[7],
					remainingPieces[8],
					remainingPieces[9],
					remainingPieces[10],
					remainingPieces[11],
			};

			Piece[] adversaries = piece.adversaries();

			long victim = (move & newState[piece.index]) ^ move;

			newState[piece.index] ^= move;
			for (Piece adversary : adversaries) {
				if ((newState[adversary.index] & victim) != 0) {
					newState[adversary.index] = newState[adversary.index] ^ victim;
					newPieceCounts[adversary.index]--;
				}
			}

			return new State(newState, newPieceCounts);
		}

		private long calculateAllBlackPieces() {
			long pieces = 0;
			for (Piece piece : Piece.getBlack()) {
				pieces |= state[piece.index];
			}
			return pieces;
		}

		private long calculateAllWhitePieces() {
			long pieces = 0;
			for (Piece piece : Piece.getWhite()) {
				pieces |= state[piece.index];
			}
			return pieces;
		}

		private int calculateRemainingBlackPieces() {
			return Long.bitCount(getAllBlackPieces());
		}

		private int calculateRemainingWhitePieces() {
			return Long.bitCount(getAllWhitePieces());
		}

		public int remainingPieces(Player player) {
			if (player.isWhite()) {
				return getRemainingWhitePieces();
			} else {
				return getRemainingBlackPieces();
			}
		}

		public int remainingPieces(Piece piece) {
			if (remainingPieces[piece.index] < 0) {
				remainingPieces[piece.index] = Long.bitCount(state[piece.index]);
			}

			return remainingPieces[piece.index];
		}

		public Piece victim(Piece attacker, long move) {
			long victim = (move & state[attacker.index]) ^ move;
			for (Piece adversary : attacker.adversaries()) {
				if ((victim & state[adversary.index]) != 0)	{
					return adversary;
				}
			}
			return null;
		}

		public boolean isTerminal() {
			return (remainingPieces(Piece.BLACK_KING) == 0 || remainingPieces(Piece.WHITE_KING) == 0);
		}

		private Board createBoard() {
			return new Board(state);
		}

		public Collection<Move> getAvailableMoves(Player player) {
			return getBoard().getAllLegalMoves(player);
		}

	}

}
