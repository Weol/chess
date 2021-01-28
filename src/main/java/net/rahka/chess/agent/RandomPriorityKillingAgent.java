package net.rahka.chess.agent;

import lombok.RequiredArgsConstructor;
import net.rahka.chess.configuration.Configurable;
import net.rahka.chess.game.Move;
import net.rahka.chess.game.Piece;
import net.rahka.chess.game.Player;
import net.rahka.chess.game.State;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * If it can kill an enemy piece, then it does so, otherwise it makes random moves. If it has more than one option
 * of pieces to kill it will kill the one with the highest value
 */
@Configurable
@RequiredArgsConstructor
public class RandomPriorityKillingAgent implements Agent {

	private int getPieceValue(Piece piece) {
		switch (piece) {
			case WHITE_PAWN:
			case BLACK_PAWN:
				return 1;
			case WHITE_ROOK:
			case BLACK_ROOK:
			case BLACK_KNIGHT:
			case BLACK_BISHOP:
			case WHITE_KNIGHT:
			case WHITE_BISHOP:
				return 2;
			case WHITE_QUEEN:
			case BLACK_QUEEN:
				return 3;
			case WHITE_KING:
			case BLACK_KING:
				return 4;
		}
		return 0;
	}

	@Override
	public Move getMove(Player player, Iterator<Move> moves, State state) {
		List<ArrayList<Move>> sorted = Arrays.asList(
				new ArrayList<>(16),
				new ArrayList<>(16),
				new ArrayList<>(16),
				new ArrayList<>(16),
				new ArrayList<>(16)
		);

		while (moves.hasNext()) {
			Move move = moves.next();

			Piece piece = state.victim(move.piece, move.move);
			if (piece != null) {
				sorted.get(getPieceValue(piece)).add(move);
			} else {
				sorted.get(0).add(move);
			}
		}

		for (int i = sorted.size() - 1; i >= 0; i--) {
			if (!sorted.get(i).isEmpty()) {
				int index = (int) (Math.random() * sorted.get(i).size());
				return sorted.get(i).get(index);
			}
		}
		return null;
	}


}
