package net.rahka.chess;

import com.sun.javafx.css.StyleManager;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.Getter;
import net.rahka.chess.agent.Agent;
import net.rahka.chess.agent.MiniMaxAgent;
import net.rahka.chess.configuration.Configurable;
import net.rahka.chess.configuration.ConfigurableClass;
import net.rahka.chess.configuration.Configuration;
import net.rahka.chess.game.Move;
import net.rahka.chess.game.Piece;
import net.rahka.chess.game.Player;
import net.rahka.chess.game.State;
import net.rahka.chess.visualizer.Visualizer;
import net.rahka.parameters.CollectionFlag;
import net.rahka.parameters.FunctionFlag;
import net.rahka.parameters.ParameterInterpreter;

import java.lang.reflect.InvocationTargetException;
import java.time.Duration;
import java.util.*;

public class Application extends javafx.application.Application {

	private static Application application;
	private static Configuration configuration;

	private static Random random = new Random();

	public static void main(String[] args) throws InterruptedException, InvocationTargetException, IllegalAccessException, InstantiationException {
		configuration = new Configuration("net.rahka.chess", Application.class);

		if (args.length > 0) {
			var agents = configuration.find(Agent.class);

			ParameterInterpreter interpreter = new ParameterInterpreter(
				new CollectionFlag<>("black", "b", "The agent of the black player", agents, true),
				new CollectionFlag<>("white", "w", "The agent of the white player", agents, true),
				new FunctionFlag<>("number", "n", "Number of games to play", Integer::valueOf, true),
				new FunctionFlag<>("seed", "s", "The seed to random generators", Long::valueOf)
			);
			var interpretation = interpreter.interpret(args);

			random = new Random(interpretation.get("seed", new Random().nextLong()));

			final ConfigurableClass<Agent> blackAgent = interpretation.get("black");
			final ConfigurableClass<Agent> whiteAgent = interpretation.get("white");

			final int games = interpretation.get("number");

			var black = blackAgent.build();
			var white = whiteAgent.build();

			long current = System.currentTimeMillis();
			CLI.run(black, white, games);
			String duration = Duration.ofMillis(current).toString()
					.substring(2)
					.replaceAll("(\\d[HMS])(?!$)", "$1 ")
					.toLowerCase();
		} else {
			Application.launch();
		}
	}

	@Configurable
	public static Random random() {
		return new Random(random.nextLong());
	}

	@Configurable
	public static Application application() {
		return application;
	}

	@Getter
	private Visualizer visualizer;

	private final OptionalMove pendingMove = new OptionalMove();

	@Override
	public void init() throws Exception {
		super.init();

		application = this;
	}

	@Override
	public void start(Stage stage) throws Exception {
		javafx.application.Application.setUserAgentStylesheet(javafx.application.Application.STYLESHEET_MODENA);
		StyleManager.getInstance().addUserAgentStylesheet("application.css");

		stage.setTitle("Chess");
		stage.setMinWidth(700);
		stage.setMinHeight(700);
		stage.setWidth(700);
		stage.setHeight(700);
		stage.getIcons().add(IO.image(IO.Images.WHITE_KNIGHT));

		visualizer = new Visualizer();
		visualizer.setPieceMoveHandler(this::onChessPieceMoved);

		visualizer.getAgentConfigurables().addAll(configuration.find(Agent.class));

		visualizer.blackAgentHolderProperty().addListener(this::onAgentChosen);
		visualizer.whiteAgentHolderProperty().addListener(this::onAgentChosen);

		var optionalAgentHolder = visualizer.getAgentConfigurables().stream()
			.filter((configurable) -> configurable.getCls().equals(MiniMaxAgent.class))
			.findAny();
		optionalAgentHolder.ifPresent(visualizer::setBlackAgentHolder);
		optionalAgentHolder.ifPresent(visualizer::setWhiteAgentHolder);

		stage.setOnCloseRequest(t -> {
			Platform.exit();
			System.exit(0);
		});

		stage.setScene(new Scene(visualizer));
		stage.show();
	}

	private void onAgentChosen(Observable observable, ConfigurableClass<Agent> old, ConfigurableClass<Agent> now) {
		if (getVisualizer().getBlackAgentHolder().getCls().equals(HumanAgent.class) || getVisualizer().getBlackAgentHolder().getCls().equals(HumanAgent.class)) {
			getVisualizer().setPlaybackControlsDisabled(true);
		} else {
			getVisualizer().setPlaybackControlsDisabled(false);
		}
	}

	private void onChessPieceMoved(Piece piece, int fromX, int fromY, int toX, int toY) {
		pendingMove.set(new Move(piece, fromX, fromY, toX, toY));

		synchronized (pendingMove) {
			pendingMove.notifyAll();
		}
	}

	@Configurable(name = "Human")
	public class HumanAgent implements Agent {

		@Override
		public Move getMove(Player player, Collection<Move> moves, State state) {
			Set<Move> validMoves = new HashSet<>(moves);

			getVisualizer().setCanSelectBlackPieces(player.isBlack());
			getVisualizer().setCanSelectWhitePieces(player.isWhite());

			Move[] matchingMoves;
			do {
				pendingMove.clear();
				synchronized (pendingMove) {
					try {
						pendingMove.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
						return null;
					}
				}
				matchingMoves = validMoves.stream().filter(move -> matches(pendingMove.get(), move)).toArray(Move[]::new);
			} while (!pendingMove.isPresent() || matchingMoves.length == 0);

			if (matchingMoves.length > 1) {
				Piece[] pieces = Arrays.stream(matchingMoves).map(Move::getSpawn).toArray(Piece[]::new);
				pendingMove.get().setSpawn(visualizer.showPiecePicker(pieces));
			}

			return pendingMove.get();
		}

		private boolean matches(Move m1, Move m2) {
			return (m1.move == m2.move && m1.piece == m2.piece);
		}

	}

	private static class OptionalMove {

		private Move move;

		void clear() {move = null;}

		void set(Move move) {this.move = move;}

		Move get() {return this.move;}

		boolean isPresent() {return (this.move != null);}

	}

}
