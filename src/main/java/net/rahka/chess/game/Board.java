package net.rahka.chess.game;

import net.rahka.chess.utils.NullFilteredIterator;

import java.util.Iterator;

public class Board {

	private long[] state;

	public Board() {
		state = new long[12];
		reset();
	}

	public Board(long[] state) {
		this.state = state;
	}

	public Board(Board board) {
		this.state = new long[] {
			board.state[0],
			board.state[1],
			board.state[2],
			board.state[3],
			board.state[4],
			board.state[5],
			board.state[6],
			board.state[7],
			board.state[8],
			board.state[9],
			board.state[10],
			board.state[11],
		};
	}

	public void reset() {
		state[Piece.WHITE_PAWN.index]   = 0b00000000_00000000_00000000_00000000_00000000_00000000_11111111_00000000L;
		state[Piece.BLACK_PAWN.index]   = 0b00000000_11111111_00000000_00000000_00000000_00000000_00000000_00000000L;
		state[Piece.WHITE_ROOK.index]   = 0b00000000_00000000_00000000_00000000_00000000_00000000_00000000_10000001L;
		state[Piece.BLACK_ROOK.index]   = 0b10000001_00000000_00000000_00000000_00000000_00000000_00000000_00000000L;
		state[Piece.WHITE_KNIGHT.index] = 0b00000000_00000000_00000000_00000000_00000000_00000000_00000000_01000010L;
		state[Piece.BLACK_KNIGHT.index] = 0b01000010_00000000_00000000_00000000_00000000_00000000_00000000_00000000L;
		state[Piece.WHITE_BISHOP.index] = 0b00000000_00000000_00000000_00000000_00000000_00000000_00000000_00100100L;
		state[Piece.BLACK_BISHOP.index] = 0b00100100_00000000_00000000_00000000_00000000_00000000_00000000_00000000L;
		state[Piece.WHITE_QUEEN.index]  = 0b00000000_00000000_00000000_00000000_00000000_00000000_00000000_00010000L;
		state[Piece.BLACK_QUEEN.index]  = 0b00001000_00000000_00000000_00000000_00000000_00000000_00000000_00000000L;
		state[Piece.WHITE_KING.index]   = 0b00000000_00000000_00000000_00000000_00000000_00000000_00000000_00001000L;
		state[Piece.BLACK_KING.index]   = 0b00010000_00000000_00000000_00000000_00000000_00000000_00000000_00000000L;
	}

	public long[] getState() {
		return state;
	}

	public long getState(Piece piece) {
		return state[piece.index];
	}

	public void move(Piece piece, long move) {
		Piece[] adversaries = piece.adversaries();

		long victim = (move & state[piece.index]) ^ move;

		state[piece.index] ^= move;
		for (Piece adversary : adversaries) {
			if ((state[adversary.index] & victim) != 0) {
				state[adversary.index] = state[adversary.index] ^ victim;
			}
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

	public static long kernelOf(int x, int y) {
		return (-0x8000000000000000L >>> (63 - (y * 8 + x)));
	}

	public Iterator<Move> getAllLegalMoves(Piece piece, int x, int y) {
		Move[] moves = new Move[piece.cacheSize];
		getMoves(moves, 0, piece, kernelOf(x, y));

		return new NullFilteredIterator<>(moves);
	}

	public Iterator<Move> getAllLegalMoves(Player player) {
		Move[] moves = new Move[140];
		int index = 0;

		if (player.isWhite()) {
			for (int y = 0; y < 8; y++) {
				for (int x = 0; x < 8; x++) {
					index = getLegalMoves(moves, index, Piece.WHITE_PAWN, x, y);
					index = getLegalMoves(moves, index, Piece.WHITE_BISHOP, x, y);
					index = getLegalMoves(moves, index, Piece.WHITE_KING, x, y);
					index = getLegalMoves(moves, index, Piece.WHITE_KNIGHT, x, y);
					index = getLegalMoves(moves, index, Piece.WHITE_QUEEN, x, y);
					index = getLegalMoves(moves, index, Piece.WHITE_ROOK, x, y);
				}
			}
		} else {
			for (int y = 0; y < 8; y++) {
				for (int x = 0; x < 8; x++) {
					index = getLegalMoves(moves, index, Piece.BLACK_PAWN, x, y);
					index = getLegalMoves(moves, index, Piece.BLACK_BISHOP, x, y);
					index = getLegalMoves(moves, index, Piece.BLACK_KING, x, y);
					index = getLegalMoves(moves, index, Piece.BLACK_KNIGHT, x, y);
					index = getLegalMoves(moves, index, Piece.BLACK_QUEEN, x, y);
					index = getLegalMoves(moves, index, Piece.BLACK_ROOK, x, y);
				}
			}
		}

		return new NullFilteredIterator<>(moves);
	}

	private int getLegalMoves(Move[] moves, int index, Piece piece, int x, int y) {
		long pieces = getState(piece);

		long kernel = kernelOf(x, y);
		if ((pieces & kernel) != 0) {
			index = getMoves(moves, index, piece, kernel);
		}
		return index;
	}

	private int getMoves(Move[] moves, int index, Piece piece, long kernel) {
		long white = getAllWhitePieces();
		long black = getAllBlackPieces();
		switch (piece) {
			case WHITE_PAWN:
				index = getWhitePawnMoves(moves, index, piece, kernel);
				break;
			case WHITE_ROOK:
				index = getRookMoves(moves, index, piece, kernel, white, black);
				break;
			case WHITE_KNIGHT:
				index = getKnightMoves(moves, index, piece, kernel, white, black);
				break;
			case WHITE_BISHOP:
				index = getBishopMoves(moves, index, piece, kernel, white, black);
				break;
			case WHITE_QUEEN:
				index = getBishopMoves(moves, index, piece, kernel, white, black);
				index = getRookMoves(moves, index, piece, kernel, white, black);
				break;
			case WHITE_KING:
				index = getKingMoves(moves, index, piece, kernel, white, black);
				break;
			case BLACK_PAWN:
				index = getBlackPawnMoves(moves, index, piece, kernel);
				break;
			case BLACK_ROOK:
				index = getRookMoves(moves, index, piece, kernel, black, white);
				break;
			case BLACK_KNIGHT:
				index = getKnightMoves(moves, index, piece, kernel, black, white);
				break;
			case BLACK_BISHOP:
				index = getBishopMoves(moves, index, piece, kernel, black, white);
				break;
			case BLACK_QUEEN:
				index = getBishopMoves(moves, index, piece, kernel, black, white);
				index = getRookMoves(moves, index, piece, kernel, black, white);
				break;
			case BLACK_KING:
				index = getKingMoves(moves, index, piece, kernel, black, white);
				break;
		}
		return index;
	}

	private int getWhitePawnMoves(Move[] moves, int index, Piece piece, long current) {
		long allied = getAllWhitePieces();
		long enemy = getAllBlackPieces();

		long all = enemy | allied;

		long next = current << 8;
		if (current < 72057594037927936L && (next & all) == 0) {
			moves[index++] = new Move(piece,  current | next);
		}

		long doubleNext = current << 16;
		if (current > 128 && current < 65536 && (next & all) == 0 && (doubleNext & all) == 0) {
			moves[index++] = new Move(piece,  current | doubleNext);
		}

		long nextRight = current << 7;
		long nextLeft = current << 9;
		if ((current & 0b11111111_00000001_00000001_00000001_00000001_00000001_00000001_00000001L) == 0 && (nextRight & allied) == 0 && (enemy & nextRight) != 0) {
			moves[index++] = new Move(piece,  current | nextRight);
		}

		if ((current & 0b11111111_10000000_10000000_10000000_10000000_10000000_10000000_10000000L) == 0 && (nextLeft & allied) == 0 && (enemy & nextLeft) != 0) {
			moves[index++] = new Move(piece,  current | nextLeft);
		}

		return index;
	}

	private int getBlackPawnMoves(Move[] moves, int index, Piece piece, long current) {
		long enemy = getAllWhitePieces();
		long allied = getAllBlackPieces();

		long all = enemy | allied;

		long next = current >>> 8;
		if (current > 128 && (next & all) == 0) {
			moves[index++] = new Move(piece,  current | next);
		}

		long doubleNext = current >>> 16;
		if (current < 72057594037927936L && current > 140737488355328L && (next & all) == 0 && (doubleNext & all) == 0) {
			moves[index++] = new Move(piece,  current | doubleNext);
		}

		long nextRight = current >>> 9;
		long nextLeft = current >>> 7;
		if ((current & 0b00000001_00000001_00000001_00000001_00000001_00000001_00000001_11111111L) == 0 && (nextRight & allied) == 0 && (enemy & nextRight) != 0) {
			moves[index++] = new Move(piece,  current | nextRight);
		}

		if ((current & 0b00000001_10000000_10000000_10000000_10000000_10000000_10000000_11111111L) == 0 && (nextLeft & allied) == 0 && (enemy & nextLeft) != 0) {
			moves[index++] = new Move(piece,  current | nextLeft);
		}

		return index;
	}

	private int getRookMoves(Move[] moves, int index, Piece piece, long current, long allied, long enemy) {
		{
			long next = current;
			while (next < 72057594037927936L && next > 0) {
				next = next << 8;

				if ((next & allied) != 0) break;
				moves[index++] = new Move(piece,  current | next);

				if ((next & enemy) != 0) break;
			}
		}

		{
			long next = current;
			while (next > 128L) {
				next = next >>> 8;

				if ((next & allied) != 0) break;
				moves[index++] = new Move(piece,  current | next);

				if ((next & enemy) != 0) break;
			}
		}

		{
			long next = current;
			while ((0b00000001_00000001_00000001_00000001_00000001_00000001_00000001_00000001L & next) == 0) {
				next = next >>> 1;

				if ((next & allied) != 0) break;
				moves[index++] = new Move(piece,  current | next);

				if ((next & enemy) != 0) break;
			}
		}

		{
			long next = current;
			while ((0b10000000_10000000_10000000_10000000_10000000_10000000_10000000_10000000L & next) == 0) {
				next = next << 1;

				if ((next & allied) != 0) break;
				moves[index++] = new Move(piece,  current | next);

				if ((next & enemy) != 0) break;
			}
		}

		return index;
	}

	private int getBishopMoves(Move[] moves, int index, Piece piece, long current, long allied, long enemy) {
		{
			long next = current;
			while ((0b11111111_00000001_00000001_00000001_00000001_00000001_00000001_00000001L & next) == 0) {
				next = next << 7;

				if ((next & allied) != 0) break;
				moves[index++] = new Move(piece,  current | next);

				if ((next & enemy) != 0) break;
			}
		}

		{
			long next = current;
			while ((0b10000000_10000000_10000000_10000000_10000000_10000000_10000000_11111111L & next) == 0) {
				next = next >>> 7;

				if ((next & allied) != 0) break;
				moves[index++] = new Move(piece,  current | next);

				if ((next & enemy) != 0) break;
			}
		}

		{
			long next = current;
			while ((0b00000001_00000001_00000001_00000001_00000001_00000001_00000001_11111111L & next) == 0) {
				next = next >>> 9;

				if ((next & allied) != 0) break;
				moves[index++] = new Move(piece,  current | next);

				if ((next & enemy) != 0) break;
			}
		}

		{
			long next = current;
			while ((0b11111111_10000000_10000000_10000000_10000000_10000000_10000000_10000000L & next) == 0) {
				next = next << 9;

				if ((next & allied) != 0) break;
				moves[index++] = new Move(piece,  current | next);

				if ((next & enemy) != 0) break;
			}
		}

		return index;
	}

	private int getKnightMoves(Move[] moves, int index, Piece piece, long current, long allied, long enemy) {
		if ((0b00000011_00000011_00000011_00000011_00000011_00000011_00000011_00000011L & current) == 0) {
			if ((0b11111111_00000000_00000000_00000000_00000000_00000000_00000000_00000000L & current) == 0) {
				long rightDown = current << 6;

				if ((rightDown & allied) == 0) moves[index++] = new Move(piece,  current | rightDown);
			}

			if ((0b00000000_00000000_00000000_00000000_00000000_00000000_00000000_11111111L & current) == 0) {
				long rightUp = current >>> 10;

				if ((rightUp & allied) == 0) moves[index++] = new Move(piece,  current | rightUp);
			}
		}

		if ((0b11000000_11000000_11000000_11000000_11000000_11000000_11000000_11000000L & current) == 0) {
			if ((0b11111111_00000000_00000000_00000000_00000000_00000000_00000000_00000000L & current) == 0) {
				long leftDown = current << 10;

				if ((leftDown & allied) == 0) moves[index++] = new Move(piece,  current | leftDown);
			}

			if ((0b00000000_00000000_00000000_00000000_00000000_00000000_00000000_11111111L & current) == 0) {
				long leftUp = current >>> 6;

				if ((leftUp & allied) == 0) moves[index++] = new Move(piece,  current | leftUp);
			}
		}

		if ((0b11111111_11111111_00000000_00000000_00000000_00000000_00000000_00000000L & current) == 0) {
			if ((0b10000000_10000000_10000000_10000000_10000000_10000000_10000000_10000000L & current) == 0) {
				long downLeft = current << 17;

				if ((downLeft & allied) == 0) moves[index++] = new Move(piece,  current | downLeft);
			}

			if ((0b00000001_00000001_00000001_00000001_00000001_00000001_00000001_00000001L & current) == 0) {
				long downRight = current << 15;

				if ((downRight & allied) == 0) moves[index++] = new Move(piece,  current | downRight);
			}
		}

		if ((0b00000000_00000000_00000000_00000000_00000000_00000000_11111111_11111111L & current) == 0) {
			if ((0b10000000_10000000_10000000_10000000_10000000_10000000_10000000_10000000L & current) == 0) {
				long upLeft = current >>> 15;

				if ((upLeft & allied) == 0) moves[index++] = new Move(piece,  current | upLeft);
			}

			if ((0b00000001_00000001_00000001_00000001_00000001_00000001_00000001_00000001L & current) == 0) {
				long upRight = current >>> 17;

				if ((upRight & allied) == 0) moves[index++] = new Move(piece,  current | upRight);
			}
		}

		return index;
	}

	private int getKingMoves(Move[] moves, int index, Piece piece, long current, long allied, long enemy) {
		if ((0b00000001_00000001_00000001_00000001_00000001_00000001_00000001_00000001L & current) == 0) {
			long right = current >>> 1;

			if ((right & allied) == 0) moves[index++] = new Move(piece,  current | right);
		}

		if ((0b10000000_10000000_10000000_10000000_10000000_10000000_10000000_10000000L & current) == 0) {
			long left = current << 1;

			if ((left & allied) == 0) moves[index++] = new Move(piece,  current | left);
		}

		if ((0b11111111_00000000_00000000_00000000_00000000_00000000_00000000_00000000L & current) == 0) {
			long down = current << 8;

			if ((down & allied) == 0) moves[index++] = new Move(piece,  current | down);
		}

		if ((0b00000000_00000000_00000000_00000000_00000000_00000000_00000000_11111111L & current) == 0) {
			long up = current >>> 8;

			if ((up & allied) == 0) moves[index++] = new Move(piece,  current | up);
		}

		if ((0b11111111_00000001_00000001_00000001_00000001_00000001_00000001_00000001L & current) == 0) {
			long downRight = current << 7;

			if ((downRight & allied) == 0) moves[index++] = new Move(piece,  current | downRight);
		}

		if ((0b11111111_10000000_10000000_10000000_10000000_10000000_10000000_10000000L & current) == 0) {
			long downLeft = current << 9;

			if ((downLeft & allied) == 0) moves[index++] = new Move(piece,  current | downLeft);
		}

		if ((0b10000000_10000000_10000000_10000000_10000000_10000000_10000000_11111111L & current) == 0) {
			long upLeft = current >>> 7;

			if ((upLeft & allied) == 0) moves[index++] = new Move(piece,  current | upLeft);
		}

		if ((0b00000001_00000001_00000001_00000001_00000001_00000001_00000001_11111111L & current) == 0) {
			long upRight = current >>> 9;

			if ((upRight & allied) == 0) moves[index++] = new Move(piece,  current | upRight);
		}

		return index;
	}

}
