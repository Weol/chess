package net.rahka.chess.game;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.rahka.chess.utils.NullFilteredIterator;

import java.util.*;
import java.util.function.Consumer;

public class Board {

	public static long kernelOf(int x, int y) {
		return (-0x8000000000000000L >>> (63 - (y * 8 + x)));
	}

	private static final PieceMoves[] PIECE_MOVES = new PieceMoves[] {
			Board::getWhitePawnMoves,
			Board::getRookMoves,
			Board::getKnightMoves,
			Board::getBishopMoves,
			Board::getQueenMoves,
			Board::getKingMoves,
			Board::getBlackPawnMoves,
			Board::getRookMoves,
			Board::getKnightMoves,
			Board::getBishopMoves,
			Board::getQueenMoves,
			Board::getKingMoves,
	};

	long[] state;

	@Getter
	private State boardState;

	@Getter @Setter
	private BoardStateChangeHandler onBoardStateChangeHandler;

	public Board(long[] state) {
		this.state = Arrays.copyOf(state, 12);

		updateMoves();
	}

	public void move(Move move) {
		Piece[] adversaries = move.piece.adversaries();

		long victim = (move.move & state[move.piece.index]) ^ move.move;

		state[move.piece.index] ^= move.move;
		if (victim != 0) {
			for (Piece adversary : adversaries) {
				if ((state[adversary.index] & victim) != 0) {
					state[adversary.index] = state[adversary.index] ^ victim;
				}
			}
		}

		if (move.then != null) {
			state[move.piece.index] ^= victim;
			state[move.then.index] |= victim;
		}

		var state = updateMoves();

		if (onBoardStateChangeHandler != null) {
			onBoardStateChangeHandler.onStateChange(move, state);
		}
	}

	public long getAllPieces() {
		return  state[Piece.WHITE_PAWN.index] |
				state[Piece.WHITE_ROOK.index] |
				state[Piece.WHITE_KNIGHT.index] |
				state[Piece.WHITE_BISHOP.index] |
				state[Piece.WHITE_QUEEN.index] |
				state[Piece.WHITE_KING.index] |
				state[Piece.BLACK_PAWN.index] |
				state[Piece.BLACK_ROOK.index] |
				state[Piece.BLACK_KNIGHT.index] |
				state[Piece.BLACK_BISHOP.index] |
				state[Piece.BLACK_QUEEN.index] |
				state[Piece.BLACK_KING.index];
	}

	public long getAllWhitePieces() {
		return  state[Piece.WHITE_PAWN.index] |
				state[Piece.WHITE_ROOK.index] |
				state[Piece.WHITE_KNIGHT.index] |
				state[Piece.WHITE_BISHOP.index] |
				state[Piece.WHITE_QUEEN.index] |
				state[Piece.WHITE_KING.index];
	}

	public long getAllBlackPieces() {
		return  state[Piece.BLACK_PAWN.index] |
				state[Piece.BLACK_ROOK.index] |
				state[Piece.BLACK_KNIGHT.index] |
				state[Piece.BLACK_BISHOP.index] |
				state[Piece.BLACK_QUEEN.index] |
				state[Piece.BLACK_KING.index];
	}

	private State updateMoves() {
		long[] coveredWhitePositions = new long[] {0};
		long[] dangerousWhitePositions = new long[] {0};

		long[] coveredBlackPositions = new long[] {0};
		long[] dangerousBlackPositions = new long[] {0};

		var whiteMoves = new ArrayList<Move>(140);
		var blackMoves = new ArrayList<Move>(140);

		long whitePositions = getAllWhitePieces();
		long blackPositions = getAllBlackPieces();

		var positionalMoves = new ArrayList<Collection<Move>>(64);

		long kernel = 0b10000000_00000000_00000000_00000000_00000000_00000000_00000000_00000000L;
		while (kernel != 0b0000000_00000000_00000000_00000000_00000000_00000000_00000000_00000001L) {
			List<Move> moves = null;
			if ((kernel & whitePositions) != 0) {
				for (Piece piece : Piece.getWhite()) {
					if ((kernel & state[piece.index]) != 0) {
						moves = getMoves(whiteMoves, dangerousBlackPositions, coveredWhitePositions, piece, kernel);
					}
				}
			} else if ((kernel & blackPositions) != 0) {
				for (Piece piece : Piece.getBlack()) {
					if ((kernel & state[piece.index]) != 0) {
						moves = getMoves(blackMoves, dangerousWhitePositions, coveredBlackPositions, piece, kernel);
					}
				}
			}

			positionalMoves.add(moves);

			kernel >>>= 1;
		}

		boardState = new State(Arrays.copyOf(state, 12),
				whitePositions,
				blackPositions,
				blackPositions | whitePositions,
				whiteMoves,
				blackMoves,
				positionalMoves,
				coveredBlackPositions[0],
				dangerousBlackPositions[0],
				coveredWhitePositions[0],
				dangerousWhitePositions[0]);

		return boardState;
	}

	private List<Move> getMoves(List<Move> moves, long[] dangerousPositions, long[] coveredPositions, Piece piece, long kernel) {
		long allied, enemy;
		if (piece.isWhite()) {
			allied = getAllWhitePieces();
			enemy = getAllBlackPieces();
		} else {
			enemy = getAllWhitePieces();
			allied = getAllBlackPieces();
		}

		int startIndex = moves.size();
		PIECE_MOVES[piece.index].get(moves, dangerousPositions, coveredPositions, piece, kernel, allied, enemy);
		int endIndex = moves.size();

		return (startIndex != endIndex) ? moves.subList(startIndex, endIndex) : null;
	}

	private static void getWhitePawnMoves(List<Move> moves, long[] dangerousPositions, long[] coveredPositions, Piece piece, long current, long allied, long enemy) {
		long all = enemy | allied;

		long next = current << 8;
		if (current < 72057594037927936L && (next & all) == 0) {
			if (next > 36028797018963968L) {
				for (Piece ally : Piece.getWhite()) {
					if (ally == Piece.WHITE_KING) continue;
					var move = new Move(piece, current | next);
					move.then = ally;
					moves.add(move);
				}
			} else {
				moves.add(new Move(piece,  current | next));
			}
		}

		long doubleNext = current << 16;
		if (current > 128 && current < 65536 && (next & all) == 0 && (doubleNext & all) == 0) {
			moves.add(new Move(piece,  current | doubleNext));
		}

		long nextRight = current << 7;
		if ((current & 0b11111111_00000001_00000001_00000001_00000001_00000001_00000001_00000001L) == 0 && (nextRight & allied) == 0) {
			if ((nextRight & enemy) != 0) {
				if (next > 36028797018963968L) {
					for (Piece ally : Piece.getWhite()) {
						if (ally == Piece.WHITE_KING) continue;
						var move = new Move(piece, current | nextRight);
						move.then = ally;
						moves.add(move);
					}
				} else {
					moves.add(new Move(piece, current | nextRight));
				}
				dangerousPositions[0] |= nextRight;
			}
		} else if ((nextRight & allied) != 0) {
			coveredPositions[0] |= nextRight;
		}

		long nextLeft = current << 9;
		if ((current & 0b11111111_10000000_10000000_10000000_10000000_10000000_10000000_10000000L) == 0 && (nextLeft & allied) == 0) {
			if ((enemy & nextLeft) != 0) {
				if (next > 36028797018963968L) {
					for (Piece ally : Piece.getWhite()) {
						if (ally == Piece.WHITE_KING) continue;
						var move = new Move(piece, current | nextLeft);
						move.then = ally;
						moves.add(move);
					}
				} else {
					moves.add(new Move(piece, current | nextLeft));
				}
				dangerousPositions[0] |= nextLeft;
			}
		} else if ((nextLeft & allied) != 0) {
			coveredPositions[0] |= nextLeft;
		}
	}

	private static void getBlackPawnMoves(List<Move> moves, long[] dangerousPositions, long[] coveredPositions, Piece piece, long current, long allied, long enemy) {
		long all = enemy | allied;

		long next = current >>> 8;
		if (current > 128 && (next & all) == 0) {
			if (current < 65536) {
				for (Piece ally : Piece.getBlack()) {
					if (ally == Piece.BLACK_KING) continue;
					var move = new Move(piece, current | next);
					move.then = ally;
					moves.add(move);
				}
			} else {
				moves.add(new Move(piece,  current | next));
			}
		}

		long doubleNext = current >>> 16;
		if (current < 72057594037927936L && current > 140737488355328L && (next & all) == 0 && (doubleNext & all) == 0) {
			moves.add(new Move(piece,  current | doubleNext));
		}

		long nextRight = current >>> 9;
		if ((current & 0b00000001_00000001_00000001_00000001_00000001_00000001_00000001_11111111L) == 0 && (nextRight & allied) == 0) {
			if ((enemy & nextRight) != 0) {
				if (current < 65536) {
					for (Piece ally : Piece.getBlack()) {
						if (ally == Piece.BLACK_KING) continue;
						var move = new Move(piece, current | nextRight);
						move.then = ally;
						moves.add(move);
					}
				} else {
					moves.add(new Move(piece, current | nextRight));
				}
				dangerousPositions[0] |= nextRight;
			}
		} else if ((nextRight & allied) != 0) {
			coveredPositions[0] |= nextRight;
		}

		long nextLeft = current >>> 7;
		if ((current & 0b00000001_10000000_10000000_10000000_10000000_10000000_10000000_11111111L) == 0 && (nextLeft & allied) == 0) {
			if ((enemy & nextLeft) != 0) {
				if (current < 65536) {
					for (Piece ally : Piece.getBlack()) {
						if (ally == Piece.BLACK_KING) continue;
						var move = new Move(piece, current | nextLeft);
						move.then = ally;
						moves.add(move);
					}
				} else {
					moves.add(new Move(piece, current | nextLeft));
				}
				dangerousPositions[0] |= nextLeft;
			}
		} else if ((nextLeft & allied) != 0) {
			coveredPositions[0] |= nextLeft;
		}
	}

	private static void getRookMoves(List<Move> moves, long[] dangerousPositions, long[] coveredPositions, Piece piece, long current, long allied, long enemy) {
		{
			long next = current;
			while (next < 72057594037927936L && next > 0) {
				next = next << 8;

				if ((next & allied) != 0) {
					coveredPositions[0] |= next;
					break;
				}

				moves.add(new Move(piece,  current | next));
				dangerousPositions[0] |= next;

				if ((next & enemy) != 0) break;
			}
		}

		{
			long next = current;
			while (next > 128L) {
				next = next >>> 8;

				if ((next & allied) != 0) {
					coveredPositions[0] |= next;
					break;
				}

				moves.add(new Move(piece,  current | next));
				dangerousPositions[0] |= next;

				if ((next & enemy) != 0) break;
			}
		}

		{
			long next = current;
			while ((0b00000001_00000001_00000001_00000001_00000001_00000001_00000001_00000001L & next) == 0) {
				next = next >>> 1;

				if ((next & allied) != 0) {
					coveredPositions[0] |= next;
					break;
				}

				moves.add(new Move(piece,  current | next));
				dangerousPositions[0] |= next;

				if ((next & enemy) != 0) break;
			}
		}

		{
			long next = current;
			while ((0b10000000_10000000_10000000_10000000_10000000_10000000_10000000_10000000L & next) == 0) {
				next = next << 1;

				if ((next & allied) != 0) {
					coveredPositions[0] |= next;
					break;
				}

				moves.add(new Move(piece,  current | next));
				dangerousPositions[0] |= next;

				if ((next & enemy) != 0) break;
			}
		}
	}

	private static void getBishopMoves(List<Move> moves, long[] dangerousPositions, long[] coveredPositions, Piece piece, long current, long allied, long enemy) {
		{
			long next = current;
			while ((0b11111111_00000001_00000001_00000001_00000001_00000001_00000001_00000001L & next) == 0) {
				next = next << 7;

				if ((next & allied) != 0) {
					coveredPositions[0] |= next;
					break;
				}

				moves.add(new Move(piece,  current | next));
				dangerousPositions[0] |= next;

				if ((next & enemy) != 0) break;
			}
		}

		{
			long next = current;
			while ((0b10000000_10000000_10000000_10000000_10000000_10000000_10000000_11111111L & next) == 0) {
				next = next >>> 7;

				if ((next & allied) != 0) {
					coveredPositions[0] |= next;
					break;
				}

				moves.add(new Move(piece,  current | next));
				dangerousPositions[0] |= next;

				if ((next & enemy) != 0) break;
			}
		}

		{
			long next = current;
			while ((0b00000001_00000001_00000001_00000001_00000001_00000001_00000001_11111111L & next) == 0) {
				next = next >>> 9;

				if ((next & allied) != 0) {
					coveredPositions[0] |= next;
					break;
				}

				moves.add(new Move(piece,  current | next));
				dangerousPositions[0] |= next;

				if ((next & enemy) != 0) break;
			}
		}

		{
			long next = current;
			while ((0b11111111_10000000_10000000_10000000_10000000_10000000_10000000_10000000L & next) == 0) {
				next = next << 9;

				if ((next & allied) != 0) {
					coveredPositions[0] |= next;
					break;
				}

				moves.add(new Move(piece,  current | next));
				dangerousPositions[0] |= next;

				if ((next & enemy) != 0) break;
			}
		}
	}

	private static void getKnightMoves(List<Move> moves, long[] dangerousPositions, long[] coveredPositions, Piece piece, long current, long allied, long enemy) {
		if ((0b00000011_00000011_00000011_00000011_00000011_00000011_00000011_00000011L & current) == 0) {
			if ((0b11111111_00000000_00000000_00000000_00000000_00000000_00000000_00000000L & current) == 0) {
				long rightDown = current << 6;

				if ((rightDown & allied) == 0) {
					moves.add(new Move(piece,  current | rightDown));
					dangerousPositions[0] |= rightDown;
				} else {
					coveredPositions[0] |= rightDown;
				}
			}

			if ((0b00000000_00000000_00000000_00000000_00000000_00000000_00000000_11111111L & current) == 0) {
				long rightUp = current >>> 10;

				if ((rightUp & allied) == 0) {
					moves.add(new Move(piece,  current | rightUp));
					dangerousPositions[0] |= rightUp;
				} else {
					coveredPositions[0] |= rightUp;
				}
			}
		}

		if ((0b11000000_11000000_11000000_11000000_11000000_11000000_11000000_11000000L & current) == 0) {
			if ((0b11111111_00000000_00000000_00000000_00000000_00000000_00000000_00000000L & current) == 0) {
				long leftDown = current << 10;

				if ((leftDown & allied) == 0) {
					moves.add(new Move(piece,  current | leftDown));
					dangerousPositions[0] |= leftDown;
				} else {
					coveredPositions[0] |= leftDown;
				}
			}

			if ((0b00000000_00000000_00000000_00000000_00000000_00000000_00000000_11111111L & current) == 0) {
				long leftUp = current >>> 6;

				if ((leftUp & allied) == 0) {
					moves.add(new Move(piece,  current | leftUp));
					dangerousPositions[0] |= leftUp;
				} else {
					coveredPositions[0] |= leftUp;
				}
			}
		}

		if ((0b11111111_11111111_00000000_00000000_00000000_00000000_00000000_00000000L & current) == 0) {
			if ((0b10000000_10000000_10000000_10000000_10000000_10000000_10000000_10000000L & current) == 0) {
				long downLeft = current << 17;

				if ((downLeft & allied) == 0) {
					moves.add(new Move(piece,  current | downLeft));
					dangerousPositions[0] |= downLeft;
				} else {
					coveredPositions[0] |= downLeft;
				}
			}

			if ((0b00000001_00000001_00000001_00000001_00000001_00000001_00000001_00000001L & current) == 0) {
				long downRight = current << 15;

				if ((downRight & allied) == 0) {
					moves.add(new Move(piece,  current | downRight));
					dangerousPositions[0] |= downRight;
				} else {
					coveredPositions[0] |= downRight;
				}
			}
		}

		if ((0b00000000_00000000_00000000_00000000_00000000_00000000_11111111_11111111L & current) == 0) {
			if ((0b10000000_10000000_10000000_10000000_10000000_10000000_10000000_10000000L & current) == 0) {
				long upLeft = current >>> 15;

				if ((upLeft & allied) == 0) {
					moves.add(new Move(piece,  current | upLeft));
					dangerousPositions[0] |= upLeft;
				} else {
					coveredPositions[0] |= upLeft;
				}
			}

			if ((0b00000001_00000001_00000001_00000001_00000001_00000001_00000001_00000001L & current) == 0) {
				long upRight = current >>> 17;

				if ((upRight & allied) == 0) {
					moves.add(new Move(piece,  current | upRight));
					dangerousPositions[0] |= upRight;
				} else {
					coveredPositions[0] |= upRight;
				}
			}
		}
	}

	private static void getQueenMoves(List<Move> moves, long[] dangerousPositions, long[] coveredPositions, Piece piece, long current, long allied, long enemy) {
		getBishopMoves(moves, dangerousPositions, coveredPositions, piece, current, allied, enemy);
		getRookMoves(moves, dangerousPositions, coveredPositions, piece, current, allied, enemy);
	}

	private static void getKingMoves(List<Move> moves, long[] dangerousPositions, long[] coveredPositions, Piece piece, long current, long allied, long enemy) {
		if ((0b00000001_00000001_00000001_00000001_00000001_00000001_00000001_00000001L & current) == 0) {
			long right = current >>> 1;

			if ((right & allied) == 0) {
				moves.add(new Move(piece,  current | right));
				dangerousPositions[0] |= right;
			} else {
				coveredPositions[0] |= right;
			}
		}

		if ((0b10000000_10000000_10000000_10000000_10000000_10000000_10000000_10000000L & current) == 0) {
			long left = current << 1;

			if ((left & allied) == 0) {
				moves.add(new Move(piece,  current | left));
				dangerousPositions[0] |= left;
			} else {
				coveredPositions[0] |= left;
			}
		}

		if ((0b11111111_00000000_00000000_00000000_00000000_00000000_00000000_00000000L & current) == 0) {
			long down = current << 8;

			if ((down & allied) == 0) {
				moves.add(new Move(piece,  current | down));
				dangerousPositions[0] |= down;
			} else {
				coveredPositions[0] |= down;
			}
		}

		if ((0b00000000_00000000_00000000_00000000_00000000_00000000_00000000_11111111L & current) == 0) {
			long up = current >>> 8;

			if ((up & allied) == 0) {
				moves.add(new Move(piece,  current | up));
				dangerousPositions[0] |= up;
			} else {
				coveredPositions[0] |= up;
			}
		}

		if ((0b11111111_00000001_00000001_00000001_00000001_00000001_00000001_00000001L & current) == 0) {
			long downRight = current << 7;

			if ((downRight & allied) == 0) {
				moves.add(new Move(piece,  current | downRight));
				dangerousPositions[0] |= downRight;
			} else {
				coveredPositions[0] |= downRight;
			}
		}

		if ((0b11111111_10000000_10000000_10000000_10000000_10000000_10000000_10000000L & current) == 0) {
			long downLeft = current << 9;

			if ((downLeft & allied) == 0) {
				moves.add(new Move(piece,  current | downLeft));
				dangerousPositions[0] |= downLeft;
			} else {
				coveredPositions[0] |= downLeft;
			}
		}

		if ((0b10000000_10000000_10000000_10000000_10000000_10000000_10000000_11111111L & current) == 0) {
			long upLeft = current >>> 7;

			if ((upLeft & allied) == 0) {
				moves.add(new Move(piece,  current | upLeft));
				dangerousPositions[0] |= upLeft;
			} else {
				coveredPositions[0] |= upLeft;
			}
		}

		if ((0b00000001_00000001_00000001_00000001_00000001_00000001_00000001_11111111L & current) == 0) {
			long upRight = current >>> 9;

			if ((upRight & allied) == 0) {
				moves.add(new Move(piece,  current | upRight));
				dangerousPositions[0] |= upRight;
			} else {
				coveredPositions[0] |= upRight;
			}
		}
	}

	private interface PieceMoves {

		void get(List<Move> moves, long[] dangerousPositions, long[] coveredPositions, Piece piece, long current, long allied, long enemy);

	}

	public interface BoardStateChangeHandler {

		void onStateChange(Move move, State state);

	}

}
