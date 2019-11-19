package net.rahka.chess.game;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.rahka.chess.BoardConfig;
import net.rahka.chess.agent.Agent;
import net.rahka.chess.game.pieces.King;
import net.rahka.chess.game.pieces.Move;
import net.rahka.chess.game.pieces.Piece;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class Chess {

	@Getter @Setter
	private Agent blackAgent, whiteAgent;

	@Getter
	private Board board;

	private ExecutorService handlerExecutor = Executors.newSingleThreadScheduledExecutor();

	private LinkedList<Consumer<Move>> onMoveHandlers = new LinkedList<>();

	public Chess(BoardConfig configuration, Agent whiteAgent, Agent blackAgent) {
		board = new Board(configuration);

		this.blackAgent = whiteAgent;
		this.whiteAgent = blackAgent;
	}

	private @NonNull State getState() {
		var state = new State();
		state.board = board;

		state.whitePieceCount = board.getWhitePieces().size();
		state.blackPieceCount = board.getBlackPieces().size();
		state.totalPieceCount = board.getAllPieces().size();

		return state;
	}

	public @NonNull Player start() {
		board.reset();

		Player winner = null;
		while (winner == null) {
			{
				var move = whiteAgent.getMove(Player.WHITE, getAllLegalMoves(Player.WHITE), getState());
				winner = move(move);
			}

			if (winner == null) {
				var move = blackAgent.getMove(Player.BLACK, getAllLegalMoves(Player.BLACK), getState());
				winner = move(move);
			}
		}
		return winner;
	}

	private Collection<Move> getAllLegalMoves(Player player) {
		final var pieces = (player == Player.WHITE) ? board.getWhitePieces() : board.getBlackPieces();
		final var moves = new ArrayList<Move>(100);

		for (Piece piece : pieces) {
			piece.getLegalMovements(getBoard()).stream().filter(Objects::nonNull).forEach(moves::add);
		}

		return moves;
	}

	public synchronized Player move(final Move move) {
		var piece = move.piece;

		board.unsetPieceAt(move.fromX, move.fromY);

		piece.setX(move.toX);
		piece.setY(move.toY);

		var victim = board.setPieceAt(piece, move.toX, move.toY);

		//var pieceName = piece.toString().replaceAll(".+\\.", "").replaceAll("@.+", "");
		//System.out.printf("%s moved %s from (%d, %d) to (%d, %d)\n", piece.getPlayer().toString(), pieceName, move.fromX, move.fromY, move.toX, move.toY);

		handlerExecutor.execute(() -> onMoveHandlers.forEach(handler -> handler.accept(move)));

		if (victim instanceof King) {
			return Player.not(victim.getPlayer());
		}
		return null;
	}

	public void addOnMoveHandler(Consumer<Move> handler) {
		onMoveHandlers.add(handler);
	}

	public void removeOnMoveHandler(Consumer<Move> handler) {
		onMoveHandlers.remove(handler);
	}

	public static class State {

		public boolean terminal = false;

		public int whitePieceCount, blackPieceCount, totalPieceCount;

		public Board board;

		public State expand(Move move) {
			var newState = new State();
			newState.whitePieceCount = whitePieceCount;
			newState.blackPieceCount = blackPieceCount;

			{
				var newBoard = new Board(board);

				var piece = move.piece;

				newBoard.unsetPieceAt(move.fromX, move.fromY);
				var victim = newBoard.setPieceAt(piece, move.toX, move.toY);

				newState.board = newBoard;

				if (victim != null) {
					if (victim.getPlayer() == Player.WHITE) whitePieceCount--;
					if (victim.getPlayer() == Player.BLACK) blackPieceCount--;
				}

				if (victim instanceof King) {
					newState.terminal = true;
				}
			}

			newState.totalPieceCount = blackPieceCount + whitePieceCount;

			return newState;
		}

		public Collection<Move> getAvailableMoves(Player player) {
			final var pieces = (player == Player.WHITE) ? board.getWhitePieces() : board.getBlackPieces();
			final var moves = new ArrayList<Move>(100);

			for (Piece piece : pieces) {
				piece.getLegalMovements(board).stream().filter(Objects::nonNull).forEach(moves::add);
			}

			return moves;
		}

	}

}
