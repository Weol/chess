package net.rahka.chess;

import net.rahka.chess.agent.Agent;
import net.rahka.chess.game.Chess;
import net.rahka.chess.game.Match;
import net.rahka.chess.game.Player;

public class CLI {

    public static void run(Agent blackAgent, Agent whiteAgent, int games) throws InterruptedException {
        Chess chess = new Chess();

        float whiteWins = 0, blackWins = 0;
        for (int i = 0; i < games; i++) {
            Match match = chess.prepare();
            match.setBlackAgent(blackAgent);
            match.setWhiteAgent(whiteAgent);

            Player player = match.getWinner();
            System.out.printf("%s won game %d / %d\n", player.toString(), i + 1, games);

            if (player == Player.BLACK) blackWins++;
            if (player == Player.WHITE) whiteWins++;
        }

        System.out.println("SUMMARY:");
        System.out.printf("Black (%s) won %.2f%% of the games (%.0f)\n", blackAgent.getClass().getSimpleName(), blackWins / games * 100, blackWins);
        System.out.printf("White (%s) won %.2f%% of the games (%.0f)\n", whiteAgent.getClass().getSimpleName(), whiteWins / games * 100, whiteWins);

        System.exit(0);
    }

}
