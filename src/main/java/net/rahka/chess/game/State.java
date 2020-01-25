package net.rahka.chess.game;

import lombok.Getter;

public class State extends Board {

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

    public State expand(Move move) {
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
        state.move(move);
        return state;
    }

    @Override
    protected void onPieceRemoved(Piece piece) {
        remainingPieces[piece.index]--;
    }

    @Override
    protected void onPieceAdded(Piece piece) {
        if (remainingPieces[piece.index] == -1) remainingPieces[piece.index] = 1; else 	remainingPieces[piece.index]++;
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