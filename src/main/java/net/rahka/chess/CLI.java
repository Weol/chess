package net.rahka.chess;

import net.rahka.chess.agent.Agent;
import net.rahka.chess.game.Chess;
import net.rahka.chess.game.Player;

public class CLI {

    public static void run(Agent blackAgent, Agent whiteAgent, int games) {
        Chess chess = new Chess(whiteAgent, blackAgent);

        float whiteWins = 0, blackWins = 0;
        for (int i = 0; i < games; i++) {
            Player winner = chess.start();
            System.out.printf("%s won game %d / %d\n", winner.toString(), i + 1, games);

            if (winner == Player.BLACK) blackWins++;
            if (winner == Player.WHITE) whiteWins++;
        }

        System.out.println("SUMMARY:");
        System.out.printf("Black won %.2f%% of the games (%.0f)\n", blackWins / games * 100, blackWins);
        System.out.printf("White won %.2f%% of the games (%.0f)\n", whiteWins / games * 100, whiteWins);
    }

}
