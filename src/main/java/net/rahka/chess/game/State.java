package net.rahka.chess.game;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
public class State {

    @Getter
    private final long[] board;

    @Getter
    private final long blackPieces, whitePieces, allPieces;

    @Getter
    private final Collection<Move> whiteMoves, blackMoves;

    private final List<Collection<Move>> positionalMoves;

    @Getter
    private final long coveredBlackPositions, dangerousBlackPositions, coveredWhitePositions, dangerousWhitePositions;

    public State expand(Move move) {
        var expandedBoard = new Board(board);
        expandedBoard.move(move);

        return expandedBoard.getBoardState();
    }

    public Collection<Move> getMoves(Player player) {
        if (player.isWhite()) {
            return whiteMoves;
        } else {
            return blackMoves;
        }
    }

    public Collection<Move> getMoves(int x, int y) {
        return positionalMoves.get(y * 8 + x);
    }

    public long getCoveredPositions(Player player) {
        if (player.isWhite()) {
            return coveredWhitePositions;
        } else {
            return coveredBlackPositions;
        }
    }

    public long getThreatenedPositions(Player player) {
        if (player.isWhite()) {
            return dangerousWhitePositions;
        } else {
            return dangerousBlackPositions;
        }
    }

    public boolean isTerminal() {
        return board[Piece.WHITE_KING.index] == 0 || board[Piece.BLACK_KING.index] == 0;
    }

    public int getPieceCount(Piece piece) {
        return Long.bitCount(board[piece.index]);
    }

}
