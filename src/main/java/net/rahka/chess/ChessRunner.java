package net.rahka.chess;

import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.restrictions.Path;
import com.github.rvesse.airline.annotations.restrictions.PathKind;
import com.github.rvesse.airline.annotations.restrictions.Required;
import net.rahka.chess.game.agent.Agent;
import net.rahka.chess.game.Match;
import net.rahka.chess.game.Player;

import java.io.File;
import java.time.Duration;
import java.util.Random;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicReference;

@Command(name = "run", description = "Run multiple games at the same time")
public class ChessRunner implements CLI.Command {

    private static final int MAX_CONCURRENT_MATCHES = 4;

    private static AtomicReference<Integer> blackWins = new AtomicReference<>(0);
    private static AtomicReference<Integer> whiteWins = new AtomicReference<>(0);

    private static Semaphore semaphore = new Semaphore(MAX_CONCURRENT_MATCHES);

    @Option(name = {"-s", "--seed"}, description = "Seed for random generation")
    private long seed = new Random().nextLong();

    @Option(name = {"--concurrent"}, description = "Max number of concurrent matches to run")
    private int maxConcurrentMatches = Runtime.getRuntime().availableProcessors();

    @Option(name = {"-n", "--games"}, description = "The total number of games to run")
    private int totalNumberOfGames = 1;

    @Option(name = {"-c", "--config"}, description = "The config file of the run")
    @Required
    @Path(mustExist = true, kind = PathKind.FILE)
    private File configurationFile;

    @Override
    public void run() throws Exception {

    }

    public static void run(Agent blackAgent, Agent whiteAgent, int games) throws InterruptedException {
        long current = System.currentTimeMillis();

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

        String duration = Duration.ofMillis(System.currentTimeMillis() - current).toString()
                .substring(2)
                .replaceAll("(\\d[HMS])(?!$)", "$1 ")
                .toLowerCase();

        System.out.printf("Matches finished %d matches in %s\n", games, duration);

        System.exit(0);
    }

    private static void onMatchStateChange(Match match, Match.State state, int i, int games) {
        if (state == Match.State.FINISHED) {
            Player player = match.getWinner();

            System.out.printf("%s won game %d / %d\n", player.toString(), i + 1, games);

            if (player == Player.BLACK) blackWins.getAndUpdate((wins) -> ++wins);
            if (player == Player.WHITE) whiteWins.getAndUpdate((wins) -> ++wins);

            semaphore.release();
        } if (state == Match.State.INTERRUPTED) {
            System.out.printf("Match %d / %d was interrupted\n", i + 1, games);
            semaphore.release();
        }
    }
}
