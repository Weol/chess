package net.rahka.chess.game;

import lombok.Getter;
import lombok.Setter;
import net.rahka.chess.agent.Agent;

import java.util.Iterator;
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
				var move = whiteAgent.getMove(Player.WHITE, board.getAllLegalMoves(Player.WHITE), new State(board));

				//int fromI = Long.numberOfTrailingZeros(board.getState(move.piece) & move.move);
				//int toI = Long.numberOfTrailingZeros((board.getState(move.piece) & move.move) ^ move.move);
				//
				//System.out.printf("White moved %s from (%d, %d) to (%d, %d)\n", move.piece, fromI % 8, fromI / 8, toI % 8, toI / 8);

				board.move(move.piece, move.move);

				runBoardChangeHandler();
				if (board.getState(Piece.BLACK_KING) == 0) {
					winner = Player.WHITE;
				}
			}

			if (winner == null) {
				var move = blackAgent.getMove(Player.BLACK, board.getAllLegalMoves(Player.BLACK), new State(board));

				//int fromI = Long.numberOfLeadingZeros(board.getState(move.piece) & move.move);
				//int toI = Long.numberOfLeadingZeros((board.getState(move.piece) & move.move) ^ move.move);
//
				//System.out.printf("Black moved %s from (%d, %d) to (%d, %d)\n", move.piece, fromI % 8, fromI / 8, toI % 8, toI / 8);

				board.move(move.piece, move.move);

				runBoardChangeHandler();
				if (board.getState(Piece.WHITE_KING) == 0) {
					winner = Player.BLACK;
				}
			}
		}
		return winner;
	}

	private void runBoardChangeHandler() {
		long[] state = new long[12];
		for (Piece piece : Piece.values()) {
			state[piece.index] = board.getState(piece);
		}

		if (boardChangeHandler != null) handlerExecutor.submit(() -> {
			boardChangeHandler.onBoardStateChange(state);
		});
	}

	public static class State extends Board {

		private final int[] remainingPieces;

		@Getter(lazy = true)
		private final int remainingWhitePieces = calculateRemainingWhitePieces();

		@Getter(lazy = true)
		private final int remainingBlackPieces = calculateRemainingBlackPieces();

		@Getter(lazy = true)
		private final long allBlackPieces = calculateAllBlackPieces();

		@Getter(lazy = true)
		private final long allWhitePieces = calculateAllWhitePieces();

		public State(Board board, int[] remainingPieces) {
			super(board);
			this.remainingPieces = remainingPieces;
		}

		public State(Board board) {
			this(board, new int[] {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1});
		}

		public State expand(Piece piece, long move) {
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

			State state = new State(this, newPieceCounts);
			state.move(piece, move);
			return state;
		}

		private long calculateAllBlackPieces() {
			long pieces = 0;
			for (Piece piece : Piece.getBlack()) {
				pieces |= getState(piece);
			}
			return pieces;
		}

		private long calculateAllWhitePieces() {
			long pieces = 0;
			for (Piece piece : Piece.getWhite()) {
				pieces |= getState(piece);
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
				remainingPieces[piece.index] = Long.bitCount(getState(piece));
			}

			return remainingPieces[piece.index];
		}

		public Piece victim(Piece attacker, long move) {
			long victim = (move & getState(attacker)) ^ move;
			for (Piece adversary : attacker.adversaries()) {
				if ((victim & getState(adversary)) != 0)	{
					return adversary;
				}
			}
			return null;
		}

		public boolean isTerminal() {
			return (remainingPieces(Piece.BLACK_KING) == 0 || remainingPieces(Piece.WHITE_KING) == 0);
		}

	}

}
