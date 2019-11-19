package net.rahka.chess.agent.heuristics;

import net.rahka.chess.game.Chess;
import net.rahka.chess.game.Player;
import net.rahka.chess.game.pieces.*;

public class RemainingPiecesHeuristic implements Heuristic {

    private int getPieceValue(Piece piece) {
        if (piece instanceof Pawn) {
            return 1;
        } else if (piece instanceof Bishop) {
            return 4;
        } else if (piece instanceof King) {
            return 100;
        } else if (piece instanceof Queen) {
            return 15;
        } else if (piece instanceof Knight) {
            return 6;
        } else if (piece instanceof Rook) {
            return 5;
        }
        return 0;
    }

    @Override
    public int heuristic(Player player, Chess.State state) {
        if (state.terminal && state.board.getAllPieces().stream().filter(piece -> piece.getPlayer() != player).noneMatch(piece -> piece instanceof King)) return 10000000;
        var friendlyScore = state.board.getAllPieces().stream().filter(piece -> piece.getPlayer() == player).mapToInt(this::getPieceValue).sum();
        var enemyScore = state.board.getAllPieces().stream().filter(piece -> piece.getPlayer() != player).mapToInt(this::getPieceValue).sum();

        return -enemyScore + friendlyScore;
    }

}
