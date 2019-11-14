package net.rahka.chess;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.rahka.agent.Agent;
import net.rahka.chess.pieces.King;
import net.rahka.chess.pieces.Move;
import net.rahka.chess.pieces.Piece;

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

		return state;
	}

	public @NonNull Player start() {
		board.reset();

		Player winner = null;
		while (winner == null) {
			{
				var move = whiteAgent.getMove(getAllLegalMoves(Player.WHITE), getState());
				winner = move(move);
			}

			if (winner == null) {
				var move = blackAgent.getMove(getAllLegalMoves(Player.BLACK), getState());
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
		var victim = board.setPieceAt(piece, move.toX, move.toY);

		var pieceName = piece.toString().replaceAll(".+\\.", "").replaceAll("@.+", "");
		System.out.printf("%s moved %s from (%d, %d) to (%d, %d)\n", piece.getPlayer().toString(), pieceName, move.fromX, move.fromY, move.toX, move.toY);

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

		public int whitePieceCount, blackPieceCount, totalPieceCount;

		public char bishops, kings, knights, queens, rooks, pawns;

		public Board board;

		State expand(Move move) {
			return null;
		}

	}

	public static class MatchNotOverException extends RuntimeException {

		MatchNotOverException(String message) {
			super(message);
		}

	}

}
