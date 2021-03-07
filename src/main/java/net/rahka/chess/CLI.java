package net.rahka.chess;

import net.rahka.chess.agent.Agent;
import net.rahka.chess.game.Match;
import net.rahka.chess.game.Player;

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicReference;

public class CLI {

    private static final int MAX_CONCURRENT_MATCHES = 4;

    private static AtomicReference<Integer> blackWins = new AtomicReference<>(0);
    private static AtomicReference<Integer> whiteWins = new AtomicReference<>(0);

    private static Semaphore semaphore = new Semaphore(MAX_CONCURRENT_MATCHES);

    public static void onMatchStateChange(Match match, Match.State state, int i, int games) {
        if (state == Match.State.FINISHED) {
            Player player = match.getWinner();

            System.out.printf("%s won game %d / %d\n", player.toString(), i + 1, games);

            if (player == Player.BLACK) blackWins.getAndUpdate((wins) -> wins++);
            if (player == Player.WHITE) whiteWins.getAndUpdate((wins) -> wins++);

            semaphore.release();
        } if (state == Match.State.INTERRUPTED) {
            System.out.printf("Match %d / %d was interrupted\n", i + 1, games);
            semaphore.release();
        }
    }

    public static void run(Agent blackAgent, Agent whiteAgent, int games) throws InterruptedException {
        for (int i = 0; i < games; i++) {
            final Match match = new Match();
            match.setBlackAgent(blackAgent);
            match.setWhiteAgent(whiteAgent);

            final int finalI = i;
            match.setOnStateChangeHandler((state) -> onMatchStateChange(match, state, finalI, games));

            semaphore.acquire();
            match.start();
        }

        semaphore.acquire(MAX_CONCURRENT_MATCHES);

        System.out.println("SUMMARY:");
        System.out.printf("Black (%s) won %.2f%% of the games (%d)\n", blackAgent.getClass().getSimpleName(), ((float) blackWins.get()) / games * 100, blackWins.get());
        System.out.printf("White (%s) won %.2f%% of the games (%d)\n", whiteAgent.getClass().getSimpleName(), ((float) whiteWins.get())/ games * 100, whiteWins.get());

        System.exit(0);
    }

}
