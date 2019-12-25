package net.rahka.chess.game;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

@Accessors(fluent = true)
public class Board {

	@Getter
	private final Moves moves = new Moves();

	private long[] pieces;

	public Board() {
		pieces = new long[12];
		reset();
	}

	public Board(long[] state) {
		pieces = Arrays.copyOf(state, 12);
	}

	public void reset() {
		pieces[Piece.WHITE_PAWN.index]   = 0b00000000_00000000_00000000_00000000_00000000_00000000_11111111_00000000L;
		pieces[Piece.BLACK_PAWN.index]   = 0b00000000_11111111_00000000_00000000_00000000_00000000_00000000_00000000L;
		pieces[Piece.WHITE_ROOK.index]   = 0b00000000_00000000_00000000_00000000_00000000_00000000_00000000_10000001L;
		pieces[Piece.BLACK_ROOK.index]   = 0b10000001_00000000_00000000_00000000_00000000_00000000_00000000_00000000L;
		pieces[Piece.WHITE_KNIGHT.index] = 0b00000000_00000000_00000000_00000000_00000000_00000000_00000000_01000010L;
		pieces[Piece.BLACK_KNIGHT.index] = 0b01000010_00000000_00000000_00000000_00000000_00000000_00000000_00000000L;
		pieces[Piece.WHITE_BISHOP.index] = 0b00000000_00000000_00000000_00000000_00000000_00000000_00000000_00100100L;
		pieces[Piece.BLACK_BISHOP.index] = 0b00100100_00000000_00000000_00000000_00000000_00000000_00000000_00000000L;
		pieces[Piece.WHITE_QUEEN.index]  = 0b00000000_00000000_00000000_00000000_00000000_00000000_00000000_00010000L;
		pieces[Piece.BLACK_QUEEN.index]  = 0b00001000_00000000_00000000_00000000_00000000_00000000_00000000_00000000L;
		pieces[Piece.WHITE_KING.index]   = 0b00000000_00000000_00000000_00000000_00000000_00000000_00000000_00001000L;
		pieces[Piece.BLACK_KING.index]   = 0b00010000_00000000_00000000_00000000_00000000_00000000_00000000_00000000L;
	}

	public long[] getState() {
		return pieces;
	}

	public long getPieces(Piece piece) {
		return pieces[piece.index];
	}

	public void move(Piece piece, long move) {
		Piece[] adversaries = piece.adversaries();

		long victim = (move & pieces[piece.index]) ^ move;

		pieces[piece.index] ^= move;
		for (Piece adversary : adversaries) {
			if ((pieces[adversary.index] & victim) != 0) {
				pieces[adversary.index] = pieces[adversary.index] ^ victim;
			}
		}
	}

	public long getAllWhitePieces() {
		return  pieces[Piece.WHITE_PAWN.index] |
				pieces[Piece.WHITE_ROOK.index] |
				pieces[Piece.WHITE_KNIGHT.index] |
				pieces[Piece.WHITE_BISHOP.index] |
				pieces[Piece.WHITE_QUEEN.index] |
				pieces[Piece.WHITE_KING.index];
	}

	public long getAllBlackPieces() {
		return  pieces[Piece.BLACK_PAWN.index] |
				pieces[Piece.BLACK_ROOK.index] |
				pieces[Piece.BLACK_KNIGHT.index] |
				pieces[Piece.BLACK_BISHOP.index] |
				pieces[Piece.BLACK_QUEEN.index] |
				pieces[Piece.BLACK_KING.index];
	}

	private void getAllLegalMoves(List<Move> moves, Piece piece) {
		long pieces = getPieces(piece);
		for (int i = 0; i < 64; i++) {
			long kernel = (-0x8000000000000000L >>> (63 - i));
			if ((pieces & kernel) != 0) {
				moves().getMoves(moves, piece, kernel);
			}
		}
	}

	public Collection<Move> getAllLegalMoves(Piece piece, long kernel) {
		var moves = new ArrayList<Move>(100);
		moves().getMoves(moves, piece, kernel);

		return moves;
	}

	public Collection<Move> getAllLegalMoves(Player player) {
		List<Move> moves = new ArrayList<>(100);

		if (player.isWhite()) {
			getAllLegalMoves(moves, Piece.WHITE_PAWN);
			getAllLegalMoves(moves, Piece.WHITE_BISHOP);
			getAllLegalMoves(moves, Piece.WHITE_KING);
			getAllLegalMoves(moves, Piece.WHITE_KNIGHT);
			getAllLegalMoves(moves, Piece.WHITE_QUEEN);
			getAllLegalMoves(moves, Piece.WHITE_ROOK);
		} else {
			getAllLegalMoves(moves, Piece.BLACK_BISHOP);
			getAllLegalMoves(moves, Piece.BLACK_KING);
			getAllLegalMoves(moves, Piece.BLACK_PAWN);
			getAllLegalMoves(moves, Piece.BLACK_KNIGHT);
			getAllLegalMoves(moves, Piece.BLACK_QUEEN);
			getAllLegalMoves(moves, Piece.BLACK_ROOK);
		}

		return moves;
	}

	public class Moves {

		@NonNull
		public void getMoves(List<Move> moves, Piece piece, long kernel) {
			long white = getAllWhitePieces();
			long black = getAllBlackPieces();
			switch (piece) {
				case WHITE_PAWN:
					getWhitePawnMoves(moves, piece, kernel);
					break;
				case WHITE_ROOK:
					getRookMoves(moves, piece, kernel, white, black);
					break;
				case WHITE_KNIGHT:
					getKnightMoves(moves, piece, kernel, white, black);
					break;
				case WHITE_BISHOP:
					getBishopMoves(moves, piece, kernel, white, black);
					break;
				case WHITE_QUEEN:
					getBishopMoves(moves, piece, kernel, white, black);
					getRookMoves(moves, piece, kernel, white, black);
					break;
				case WHITE_KING:
					getKingMoves(moves, piece, kernel, white, black);
					break;
				case BLACK_PAWN:
					getBlackPawnMoves(moves, piece, kernel);
					break;
				case BLACK_ROOK:
					getRookMoves(moves, piece, kernel, black, white);
					break;
				case BLACK_KNIGHT:
					getKnightMoves(moves, piece, kernel, black, white);
					break;
				case BLACK_BISHOP:
					getBishopMoves(moves, piece, kernel, black, white);
					break;
				case BLACK_QUEEN:
					getBishopMoves(moves, piece, kernel, black, white);
					getRookMoves(moves, piece, kernel, black, white);
					break;
				case BLACK_KING:
					getKingMoves(moves, piece, kernel, black, white);
					break;
			}
		}

		/**
		 * (00):  00000000 00000000 00000000 00000000 00000000 00000000 00000000 00000001 (1)
		 * (01):  00000000 00000000 00000000 00000000 00000000 00000000 00000000 00000010 (2)
		 * (02):  00000000 00000000 00000000 00000000 00000000 00000000 00000000 00000100 (4)
		 * (03):  00000000 00000000 00000000 00000000 00000000 00000000 00000000 00001000 (8)
		 * (04):  00000000 00000000 00000000 00000000 00000000 00000000 00000000 00010000 (16)
		 * (05):  00000000 00000000 00000000 00000000 00000000 00000000 00000000 00100000 (32)
		 * (06):  00000000 00000000 00000000 00000000 00000000 00000000 00000000 01000000 (64)
		 * (07):  00000000 00000000 00000000 00000000 00000000 00000000 00000000 10000000 (128)
		 * (08):  00000000 00000000 00000000 00000000 00000000 00000000 00000001 00000000 (256)
		 * (09):  00000000 00000000 00000000 00000000 00000000 00000000 00000010 00000000 (512)
		 * (10):  00000000 00000000 00000000 00000000 00000000 00000000 00000100 00000000 (1024)
		 * (11):  00000000 00000000 00000000 00000000 00000000 00000000 00001000 00000000 (2048)
		 * (12):  00000000 00000000 00000000 00000000 00000000 00000000 00010000 00000000 (4096)
		 * (13):  00000000 00000000 00000000 00000000 00000000 00000000 00100000 00000000 (8192)
		 * (14):  00000000 00000000 00000000 00000000 00000000 00000000 01000000 00000000 (16384)
		 * (15):  00000000 00000000 00000000 00000000 00000000 00000000 10000000 00000000 (32768)
		 * (16):  00000000 00000000 00000000 00000000 00000000 00000001 00000000 00000000 (65536)
		 * (17):  00000000 00000000 00000000 00000000 00000000 00000010 00000000 00000000 (131072)
		 * (18):  00000000 00000000 00000000 00000000 00000000 00000100 00000000 00000000 (262144)
		 * (19):  00000000 00000000 00000000 00000000 00000000 00001000 00000000 00000000 (524288)
		 * (20):  00000000 00000000 00000000 00000000 00000000 00010000 00000000 00000000 (1048576)
		 * (21):  00000000 00000000 00000000 00000000 00000000 00100000 00000000 00000000 (2097152)
		 * (22):  00000000 00000000 00000000 00000000 00000000 01000000 00000000 00000000 (4194304)
		 * (23):  00000000 00000000 00000000 00000000 00000000 10000000 00000000 00000000 (8388608)
		 * (24):  00000000 00000000 00000000 00000000 00000001 00000000 00000000 00000000 (16777216)
		 * (25):  00000000 00000000 00000000 00000000 00000010 00000000 00000000 00000000 (33554432)
		 * (26):  00000000 00000000 00000000 00000000 00000100 00000000 00000000 00000000 (67108864)
		 * (27):  00000000 00000000 00000000 00000000 00001000 00000000 00000000 00000000 (134217728)
		 * (28):  00000000 00000000 00000000 00000000 00010000 00000000 00000000 00000000 (268435456)
		 * (29):  00000000 00000000 00000000 00000000 00100000 00000000 00000000 00000000 (536870912)
		 * (30):  00000000 00000000 00000000 00000000 01000000 00000000 00000000 00000000 (1073741824)
		 * (31):  00000000 00000000 00000000 00000000 10000000 00000000 00000000 00000000 (2147483648)
		 * (32):  00000000 00000000 00000000 00000001 00000000 00000000 00000000 00000000 (4294967296)
		 * (33):  00000000 00000000 00000000 00000010 00000000 00000000 00000000 00000000 (8589934592)
		 * (34):  00000000 00000000 00000000 00000100 00000000 00000000 00000000 00000000 (17179869184)
		 * (35):  00000000 00000000 00000000 00001000 00000000 00000000 00000000 00000000 (34359738368)
		 * (36):  00000000 00000000 00000000 00010000 00000000 00000000 00000000 00000000 (68719476736)
		 * (37):  00000000 00000000 00000000 00100000 00000000 00000000 00000000 00000000 (137438953472)
		 * (38):  00000000 00000000 00000000 01000000 00000000 00000000 00000000 00000000 (274877906944)
		 * (39):  00000000 00000000 00000000 10000000 00000000 00000000 00000000 00000000 (549755813888)
		 * (40):  00000000 00000000 00000001 00000000 00000000 00000000 00000000 00000000 (1099511627776)
		 * (41):  00000000 00000000 00000010 00000000 00000000 00000000 00000000 00000000 (2199023255552)
		 * (42):  00000000 00000000 00000100 00000000 00000000 00000000 00000000 00000000 (4398046511104)
		 * (43):  00000000 00000000 00001000 00000000 00000000 00000000 00000000 00000000 (8796093022208)
		 * (44):  00000000 00000000 00010000 00000000 00000000 00000000 00000000 00000000 (17592186044416)
		 * (45):  00000000 00000000 00100000 00000000 00000000 00000000 00000000 00000000 (35184372088832)
		 * (46):  00000000 00000000 01000000 00000000 00000000 00000000 00000000 00000000 (70368744177664)
		 * (47):  00000000 00000000 10000000 00000000 00000000 00000000 00000000 00000000 (140737488355328)
		 * (48):  00000000 00000001 00000000 00000000 00000000 00000000 00000000 00000000 (281474976710656)
		 * (49):  00000000 00000010 00000000 00000000 00000000 00000000 00000000 00000000 (562949953421312)
		 * (50):  00000000 00000100 00000000 00000000 00000000 00000000 00000000 00000000 (1125899906842624)
		 * (51):  00000000 00001000 00000000 00000000 00000000 00000000 00000000 00000000 (2251799813685248)
		 * (52):  00000000 00010000 00000000 00000000 00000000 00000000 00000000 00000000 (4503599627370496)
		 * (53):  00000000 00100000 00000000 00000000 00000000 00000000 00000000 00000000 (9007199254740992)
		 * (54):  00000000 01000000 00000000 00000000 00000000 00000000 00000000 00000000 (18014398509481984)
		 * (55):  00000000 10000000 00000000 00000000 00000000 00000000 00000000 00000000 (36028797018963968)
		 * (56):  00000001 00000000 00000000 00000000 00000000 00000000 00000000 00000000 (72057594037927936)
		 * (57):  00000010 00000000 00000000 00000000 00000000 00000000 00000000 00000000 (144115188075855872)
		 * (58):  00000100 00000000 00000000 00000000 00000000 00000000 00000000 00000000 (288230376151711744)
		 * (59):  00001000 00000000 00000000 00000000 00000000 00000000 00000000 00000000 (576460752303423488)
		 * (60):  00010000 00000000 00000000 00000000 00000000 00000000 00000000 00000000 (1152921504606846976)
		 * (61):  00100000 00000000 00000000 00000000 00000000 00000000 00000000 00000000 (2305843009213693952)
		 * (62):  01000000 00000000 00000000 00000000 00000000 00000000 00000000 00000000 (4611686018427387904)
		 * (63):  10000000 00000000 00000000 00000000 00000000 00000000 00000000 00000000 (-9223372036854775808)
		 */

		private void getWhitePawnMoves(List<Move> moves, Piece piece, long current) {
			long allied = getAllWhitePieces();
			long enemy = getAllBlackPieces();

			long all = enemy | allied;

			long next = current << 8;
			if (current < 72057594037927936L && (next & all) == 0) {
				moves.add(new Move(piece,  current | next));
			}

			long doubleNext = current << 16;
			if (current > 128 && current < 65536 && (next & all) == 0 && (doubleNext & all) == 0) {
				moves.add(new Move(piece,  current | doubleNext));
			}

			long nextRight = current << 7;
			long nextLeft = current << 9;
			if ((current & 0b11111111_00000001_00000001_00000001_00000001_00000001_00000001_00000001L) == 0 && (nextRight & allied) == 0 && (enemy & nextRight) != 0) {
				moves.add(new Move(piece,  current | nextRight));
			} else if ((current & 0b11111111_10000000_10000000_10000000_10000000_10000000_10000000_10000000L) == 0 && (nextLeft & allied) == 0 && (enemy & nextLeft) != 0) {
				moves.add(new Move(piece,  current | nextLeft));
			}
		}

		private void getBlackPawnMoves(List<Move> moves, Piece piece, long current) {
			long enemy = getAllWhitePieces();
			long allied = getAllBlackPieces();

			long all = enemy | allied;

			long next = current >>> 8;
			if (current > 128 && (next & all) == 0) {
				moves.add(new Move(piece,  current | next));
			}

			long doubleNext = current >>> 16;
			if (current < 72057594037927936L && current > 140737488355328L && (next & all) == 0 && (doubleNext & all) == 0) {
				moves.add(new Move(piece,  current | doubleNext));
			}

			long nextRight = current >>> 9;
			long nextLeft = current >>> 7;
			if ((current & 0b00000001_00000001_00000001_00000001_00000001_00000001_00000001_11111111L) == 0 && (nextRight & allied) == 0 && (enemy & nextRight) != 0) {
				moves.add(new Move(piece,  current | nextRight));
			} else if ((current & 0b00000001_10000000_10000000_10000000_10000000_10000000_10000000_11111111L) == 0 && (nextLeft & allied) == 0 && (enemy & nextLeft) != 0) {
				moves.add(new Move(piece,  current | nextLeft));
			}
		}

		private void getRookMoves(List<Move> moves, Piece piece, long current, long allied, long enemy) {
			{
				long next = current;
				while (next < 72057594037927936L && next > 0) {
					next = next << 8;

					if ((next & allied) != 0) break;
					moves.add(new Move(piece,  current | next));

					if ((next & enemy) != 0) break;
				}
			}

			{
				long next = current;
				while (next > 128L) {
					next = next >>> 8;

					if ((next & allied) != 0) break;
					moves.add(new Move(piece,  current | next));

					if ((next & enemy) != 0) break;
				}
			}

			{
				long next = current;
				while ((0b00000001_00000001_00000001_00000001_00000001_00000001_00000001_00000001L & next) == 0) {
					next = next >>> 1;

					if ((next & allied) != 0) break;
					moves.add(new Move(piece,  current | next));

					if ((next & enemy) != 0) break;
				}
			}

			{
				long next = current;
				while ((0b10000000_10000000_10000000_10000000_10000000_10000000_10000000_10000000L & next) == 0) {
					next = next << 1;

					if ((next & allied) != 0) break;
					moves.add(new Move(piece,  current | next));

					if ((next & enemy) != 0) break;
				}
			}
		}

		private void getBishopMoves(List<Move> moves, Piece piece, long current, long allied, long enemy) {
			{
				long next = current;
				while ((0b11111111_00000001_00000001_00000001_00000001_00000001_00000001_00000001L & next) == 0) {
					next = next << 7;

					if ((next & allied) != 0) break;
					moves.add(new Move(piece,  current | next));

					if ((next & enemy) != 0) break;
				}
			}

			{
				long next = current;
				while ((0b10000000_10000000_10000000_10000000_10000000_10000000_10000000_11111111L & next) == 0) {
					next = next >>> 7;

					if ((next & allied) != 0) break;
					moves.add(new Move(piece,  current | next));

					if ((next & enemy) != 0) break;
				}
			}

			{
				long next = current;
				while ((0b00000001_00000001_00000001_00000001_00000001_00000001_00000001_11111111L & next) == 0) {
					next = next >>> 9;

					if ((next & allied) != 0) break;
					moves.add(new Move(piece,  current | next));

					if ((next & enemy) != 0) break;
				}
			}

			{
				long next = current;
				while ((0b11111111_10000000_10000000_10000000_10000000_10000000_10000000_10000000L & next) == 0) {
					next = next << 9;

					if ((next & allied) != 0) break;
					moves.add(new Move(piece,  current | next));

					if ((next & enemy) != 0) break;
				}
			}
		}

		private void getKnightMoves(List<Move> moves, Piece piece, long current, long allied, long enemy) {
			long[] nexts = new long[] {-1, -1, -1, -1, -1, -1, -1, -1};

			if ((0b00000011_00000011_00000011_00000011_00000011_00000011_00000011_00000011L & current) == 0) {
				if ((0b11111111_00000000_00000000_00000000_00000000_00000000_00000000_00000000L & current) == 0) {
					long rightDown = current << 6;

					nexts[0] = rightDown;
				}

				if ((0b00000000_00000000_00000000_00000000_00000000_00000000_00000000_11111111L & current) == 0) {
					long rightUp = current >>> 10;

					nexts[1] = rightUp;
				}
			}

			if ((0b11000000_11000000_11000000_11000000_11000000_11000000_11000000_11000000L & current) == 0) {
				if ((0b11111111_00000000_00000000_00000000_00000000_00000000_00000000_00000000L & current) == 0) {
					long leftDown = current << 10;

					nexts[2] = leftDown;
				}

				if ((0b00000000_00000000_00000000_00000000_00000000_00000000_00000000_11111111L & current) == 0) {
					long leftUp = current >>> 6;

					nexts[3] = leftUp;
				}
			}

			if ((0b11111111_11111111_00000000_00000000_00000000_00000000_00000000_00000000L & current) == 0) {
				if ((0b10000000_10000000_10000000_10000000_10000000_10000000_10000000_10000000L & current) == 0) {
					long downLeft = current << 17;

					nexts[4] = downLeft;
				}

				if ((0b00000001_00000001_00000001_00000001_00000001_00000001_00000001_00000001L & current) == 0) {
					long downRight = current << 15;

					nexts[5] = downRight;
				}
			}

			if ((0b00000000_00000000_00000000_00000000_00000000_00000000_11111111_11111111L & current) == 0) {
				if ((0b10000000_10000000_10000000_10000000_10000000_10000000_10000000_10000000L & current) == 0) {
					long upLeft = current >>> 15;

					nexts[6] = upLeft;
				}

				if ((0b00000001_00000001_00000001_00000001_00000001_00000001_00000001_00000001L & current) == 0) {
					long upRight = current >>> 17;

					nexts[7] = upRight;
				}
			}

			for (long next : nexts) {
				if (next != -1) {
					if ((next & allied) == 0) {
						moves.add(new Move(piece,  current | next));
					}
				}
			}
		}

		private void getKingMoves(List<Move> moves, Piece piece, long current, long allied, long enemy) {
			long[] nexts = new long[] {-1, -1, -1, -1, -1, -1, -1, -1};;

			if ((0b00000001_00000001_00000001_00000001_00000001_00000001_00000001_00000001L & current) == 0) {
				long right = current >>> 1;

				nexts[0] = right;
			}

			if ((0b10000000_10000000_10000000_10000000_10000000_10000000_10000000_10000000L & current) == 0) {
				long left = current << 1;

				nexts[1] = left;
			}

			if ((0b11111111_00000000_00000000_00000000_00000000_00000000_00000000_00000000L & current) == 0) {
				long down = current << 8;

				nexts[2] = down;
			}

			if ((0b00000000_00000000_00000000_00000000_00000000_00000000_00000000_11111111L & current) == 0) {
				long up = current >>> 8;

				nexts[3] = up;
			}

			if ((0b11111111_00000001_00000001_00000001_00000001_00000001_00000001_00000001L & current) == 0) {
				long downRight = current << 7;

				nexts[4] = downRight;
			}

			if ((0b11111111_10000000_10000000_10000000_10000000_10000000_10000000_10000000L & current) == 0) {
				long downLeft = current << 9;

				nexts[5] = downLeft;
			}

			if ((0b10000000_10000000_10000000_10000000_10000000_10000000_10000000_11111111L & current) == 0) {
				long upLeft = current >>> 7;

				nexts[6] = upLeft;
			}

			if ((0b00000001_00000001_00000001_00000001_00000001_00000001_00000001_11111111L & current) == 0) {
				long upRight = current >>> 9;

				nexts[7] = upRight;
			}

			for (long next : nexts) {
				if (next != -1) {
					if ((next & allied) == 0) {
						moves.add(new Move(piece,  current | next));
					}
				}
			}
		}

	}

}
