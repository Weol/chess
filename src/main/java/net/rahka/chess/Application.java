package net.rahka.chess;

import com.sun.javafx.css.StyleManager;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import lombok.Getter;
import net.rahka.chess.agent.Agent;
import net.rahka.chess.agent.MiniMaxAgent;
import net.rahka.chess.configuration.Configurable;
import net.rahka.chess.configuration.ConfigurableClass;
import net.rahka.chess.configuration.Configuration;
import net.rahka.chess.game.*;
import net.rahka.chess.visualizer.Visualizer;

import java.util.*;

public class Application extends javafx.application.Application {

	private static Application application;

	public static void main(String[] args) throws InterruptedException {
		if (args.length > 0) {
					/*
			ParameterInterpreter interpreter = new ParameterInterpreter(
				new CollectionFlag<>("black", "b", "The agent of the black player", AGENT_SUPPLIERS, true),
				new CollectionFlag<>("white", "w", "The agent of the white player", AGENT_SUPPLIERS, true),
				new CollectionFlag<>("blackHeuristic", "bh", "Black agent configurations heuristic", HEURISTIC_SUPPLIERS),
				new CollectionFlag<>("whiteHeuristic", "wh", "White agent configurations heuristic", HEURISTIC_SUPPLIERS),
				new FunctionFlag<>("number", "n", "Number of games to play", Integer::valueOf, true),
				new FunctionFlag<>("depthlimit", "dl", "Agent configurations depth limit", Integer::valueOf)
			);
			var interpretation = interpreter.interpret(args);

			final HeuristicSupplier defaultHeuristicSupplier = new HeuristicSupplier("RemainingPiecesHeuristic", RemainingPiecesHeuristic::new);

			final HeuristicSupplier blackHeuristic = interpretation.get("blackHeuristic", defaultHeuristicSupplier);
			final HeuristicSupplier whiteHeuristic = interpretation.get("whiteHeuristic", defaultHeuristicSupplier);


			final AgentSupplier blackAgentSupplier = (AgentSupplier) interpretation.get("black");
			final AgentSupplier whiteAgentSupplier = (AgentSupplier) interpretation.get("white");

			final int depthLimit = interpretation.get("depthlimit", 4);

			final int games = (int) interpretation.get("number");

			CLI.run(blackAgentSupplier.getSupplier().apply(blackConfiguration), whiteAgentSupplier.getSupplier().apply(whiteConfiguration), games);
		 	**/
		} else {
			Application.launch();
		}
	}

	@Configurable
	public static Application application() {
		return application;
	}

	@Getter
	private Visualizer visualizer;

	@Getter
	private Configuration configuration;

	private final OptionalMove pendingMove = new OptionalMove();

	@Override
	public void init() throws Exception {
		super.init();

		application = this;

		configuration = new Configuration("net.rahka.chess", Application.class);
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

		visualizer.getAgentConfigurables().addAll(getConfiguration().find(Agent.class));

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
