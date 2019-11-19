package net.rahka.chess;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.rahka.chess.game.Player;
import net.rahka.chess.game.pieces.Bishop;
import net.rahka.chess.game.pieces.King;
import net.rahka.chess.game.pieces.Knight;
import net.rahka.chess.game.pieces.Pawn;
import net.rahka.chess.game.pieces.Piece;
import net.rahka.chess.game.pieces.Queen;
import net.rahka.chess.game.pieces.Rook;
import net.rahka.chess.game.pieces.Queen;

import java.util.Iterator;

@RequiredArgsConstructor
public abstract class BoardConfig {

	public static BoardConfig DEFAULT = new DefaultBoardConfig();

	@NonNull @Getter
	private int dimensions;

	public abstract int getWhitePieceCount();

	public abstract int getTotalPiecesCount();

	public abstract int getBlackPieceCount();

	public abstract Iterator<PieceConfig> configurations();

	private static class DefaultBoardConfig extends BoardConfig {

		private final PieceConfig[] pieceConfigs;

		DefaultBoardConfig() {
			super(8);

			Piece[] pieces = new Piece[8*4];

			var i = 0;
			for (int x = 0; x <= 7; x++) {
				pieces[i++] = new Pawn(Player.BLACK, x, 1);
				pieces[i++] = new Pawn(Player.WHITE, x, 6);
			}

			pieces[i++] = new Rook(Player.BLACK, 0, 0);
			pieces[i++] = new Rook(Player.WHITE, 0, 7);

			pieces[i++] = new Knight(Player.BLACK, 1, 0);
			pieces[i++] = new Knight(Player.WHITE, 1, 7);

			pieces[i++] = new Bishop(Player.BLACK, 2, 0);
			pieces[i++] = new Bishop(Player.WHITE, 2, 7);

			pieces[i++] = new Queen(Player.BLACK, 3, 0);
			pieces[i++] = new Queen(Player.WHITE, 3, 7);

			pieces[i++] = new Bishop(Player.BLACK, 5, 0);
			pieces[i++] = new Bishop(Player.WHITE, 5, 7);

			pieces[i++] = new Knight(Player.BLACK, 6, 0);
			pieces[i++] = new Knight(Player.WHITE, 6, 7);

			pieces[i++] = new Rook(Player.BLACK, 7, 0);
			pieces[i++] = new Rook(Player.WHITE, 7, 7);

			pieces[i++] = new King(Player.BLACK, 4, 0);
			pieces[i] = new King(Player.WHITE, 4, 7);

			pieceConfigs = new PieceConfig[4*8];

			for (int j = 0; j < pieces.length; j++) {
				pieceConfigs[j] = new PieceConfig(pieces[j], pieces[j].getX(), pieces[j].getY());
			}
		}

		@Override
		public int getWhitePieceCount() {
			return 16;
		}

		@Override
		public int getTotalPiecesCount() {
			return 32;
		}

		@Override
		public int getBlackPieceCount() {
			return 16;
		}

		public Iterator<PieceConfig> configurations() {
			return new Iterator<>() {

				int j = 0;

				@Override
				public boolean hasNext() {
					return j < pieceConfigs.length;
				}

				@Override
				public PieceConfig next() {
					return pieceConfigs[j++];
				}

			};
		}

	}

	@AllArgsConstructor
	public static class PieceConfig {

		@NonNull @Getter
		private Piece piece;

		@NonNull @Getter
		private int startX, startY;

	}

}
