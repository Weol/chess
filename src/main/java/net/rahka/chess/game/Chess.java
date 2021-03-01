package net.rahka.chess.game;

import net.rahka.chess.agent.Agent;

import java.util.Arrays;

public class Chess {

	public Match prepare() {
		final Match match = new Match(createDefaultBoard());
		match.setMatchState(Match.State.PREPARED);

		match.thread = new Thread(() -> {
			match.setMatchState(Match.State.ONGOING);
			Player winner = play(match, match.getWhiteAgent(), match.getBlackAgent());
			if (winner == null) {
				match.setMatchState(Match.State.INTERRUPTED);
			} else {
				match.winner = winner;
				match.setMatchState(Match.State.FINISHED);
			}
		});

		return match;
	}

	private Player play(Match match, Agent whiteAgent, Agent blackAgent) {
		Board board = match.getBoard();
		board.state = Arrays.copyOf(board.getInitialState(), 12);

		Player winner = null;
		boolean draw = false;
		while (winner == null && !Thread.currentThread().isInterrupted()) {
			{
				match.setCurrentPlayer(Player.WHITE);
				var legalMoves = board.getAllLegalMoves(Player.WHITE);
				var move = whiteAgent.getMove(Player.WHITE, legalMoves, new State(board));

				//int fromI = Long.numberOfTrailingZeros(board.getState(move.piece) & move.move);
				//int toI = Long.numberOfTrailingZeros((board.getState(move.piece) & move.move) ^ move.move);

				//System.out.printf("White moved %s from (%d, %d) to (%d, %d)\n", move.piece, fromI % 8, fromI / 8, toI % 8, toI / 8);

				board.move(move);
				match.moves.add(Arrays.copyOf(board.getState(), 12));
				runBoardChangeHandler(match);

				if (board.getState(Piece.BLACK_KING) == 0) {
					winner = Player.WHITE;
				}

				whiteAgent.postMove(move);
			}

			if (winner == null && !Thread.currentThread().isInterrupted()) {
				match.setCurrentPlayer(Player.BLACK);
				var legalMoves = board.getAllLegalMoves(Player.BLACK);
				var move = blackAgent.getMove(Player.BLACK, legalMoves, new State(board));

				//int fromI = Long.numberOfLeadingZeros(board.getState(move.piece) & move.move);
				//int toI = Long.numberOfLeadingZeros((board.getState(move.piece) & move.move) ^ move.move);

				//System.out.printf("Black moved %s from (%d, %d) to (%d, %d)\n", move.piece, fromI % 8, fromI / 8, toI % 8, toI / 8);

				board.move(move);

				match.moves.add(Arrays.copyOf(board.getState(), 12));
				runBoardChangeHandler(match);

				if (board.getState(Piece.WHITE_KING) == 0) {
					winner = Player.BLACK;
				}

				blackAgent.postMove(move);
			}

			if (winner == null && (board.getAllPieces() & (board.getState(Piece.WHITE_KING) | board.getState(Piece.WHITE_KING))) == board.getAllPieces()) {
				return null;
			}
		}
		return winner;
	}

	private Board createDefaultBoard() {
		return new Board(new long[] {
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
		});
	}

	private void runBoardChangeHandler(Match match) {
		if (match.onBoardStateChangeHandler != null) {
			long[] state = new long[12];
			for (Piece piece : Piece.values()) {
				state[piece.index] = match.getBoard().getState(piece);
			}

			match.onBoardStateChangeHandler.accept(state);
		}
	}

}
