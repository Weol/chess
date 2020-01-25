package net.rahka.chess;

import com.sun.javafx.css.StyleManager;
import javafx.beans.Observable;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.rahka.chess.agent.Agent;
import net.rahka.chess.agent.AgentConfiguration;
import net.rahka.chess.agent.heuristics.Heuristic;
import net.rahka.chess.agent.heuristics.RemainingPiecesHeuristic;
import net.rahka.chess.game.Board;
import net.rahka.chess.game.Chess;
import net.rahka.chess.game.Move;
import net.rahka.chess.game.Piece;
import net.rahka.chess.game.Player;
import net.rahka.chess.game.State;
import net.rahka.chess.visualizer.Visualizer;
import net.rahka.parameters.CollectionFlag;
import net.rahka.parameters.FunctionFlag;
import net.rahka.parameters.ParameterInterpreter;
import org.reflections.Reflections;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

@Slf4j
public class Application extends javafx.application.Application {

	@NonNull @Getter
	private static final List<AgentSupplier> AGENT_SUPPLIERS = new ArrayList<>();

	@NonNull @Getter
	private static final List<HeuristicSupplier> HEURISTIC_SUPPLIERS = new ArrayList<>();

	public static void main(String[] args) throws InterruptedException {
		loadAgents();
		loadHeuristics();

		if (args.length > 0) {
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

			final HeuristicSupplier blackHeuristic = (HeuristicSupplier) interpretation.get("blackHeuristic", defaultHeuristicSupplier);
			final HeuristicSupplier whiteHeuristic = (HeuristicSupplier) interpretation.get("whiteHeuristic", defaultHeuristicSupplier);

			final AgentSupplier blackAgentSupplier = (AgentSupplier) interpretation.get("black");
			final AgentSupplier whiteAgentSupplier = (AgentSupplier) interpretation.get("white");

			final int depthLimit = interpretation.get("depthlimit", 4);

			final int games = (int) interpretation.get("number");

			var blackConfiguration = new AgentConfiguration(depthLimit, blackHeuristic.get());
			var whiteConfiguration = new AgentConfiguration(depthLimit, whiteHeuristic.get());

			CLI.run(blackAgentSupplier.getSupplier().apply(blackConfiguration), whiteAgentSupplier.getSupplier().apply(whiteConfiguration), games);
		} else {
			Application.launch();
		}
	}

	@Getter
	private Visualizer visualizer;

	@Getter
	private Chess chess;

	private final OptionalMove pendingMove = new OptionalMove();

	@Override
	public void init() throws Exception {
		super.init();

		chess = new Chess();
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

		visualizer = new Visualizer(() -> getChess().prepare());
		visualizer.setPieceMoveHandler(this::onChessPieceMoved);

		visualizer.getAgentSuppliers().addAll(AGENT_SUPPLIERS);
		visualizer.getHeuristicSuppliers().addAll(HEURISTIC_SUPPLIERS);

		visualizer.getAgentSuppliers().add(new AgentSupplier((config) -> new HumanAgent(), "Human"));
		visualizer.blackAgentHolderProperty().addListener(this::onAgentChosen);
		visualizer.whiteAgentHolderProperty().addListener(this::onAgentChosen);

		var optionalAgentHolder = visualizer.getAgentSuppliers().stream()
			.filter((holder) -> holder.getName().equals("MiniMaxAgent"))
			.findAny();
		optionalAgentHolder.ifPresent(visualizer::setBlackAgentHolder);
		optionalAgentHolder.ifPresent(visualizer::setWhiteAgentHolder);

		var optionalHeuristicHolder = visualizer.getHeuristicSuppliers().stream()
			.filter((holder) -> holder.getName().equals("RemainingPiecesHeuristic"))
			.findAny();
		optionalHeuristicHolder.ifPresent(visualizer::setBlackHeuristicHolder);
		optionalHeuristicHolder.ifPresent(visualizer::setWhiteHeuristicHolder);

		stage.setScene(new Scene(visualizer));
		stage.show();
	}

	private void onAgentChosen(Observable observable, AgentSupplier old, AgentSupplier now) {
		if (getVisualizer().getBlackAgentHolder().getName().equals("Human") || getVisualizer().getWhiteAgentHolder().getName().equals("Human")) {
			getVisualizer().setPlaybackControlsDisabled(true);
		} else {
			getVisualizer().setPlaybackControlsDisabled(false);
		}
	}

	private void onChessPieceMoved(Piece piece, int fromX, int fromY, int toX, int toY) {
		long move = Board.kernelOf(fromX, fromY) | Board.kernelOf(toX, toY);
		pendingMove.set(new Move(piece, move));

		synchronized (pendingMove) {
			pendingMove.notifyAll();
		}
	}

	private static void loadAgents() {
		{
			Reflections reflections = new Reflections("net.rahka.chess.agent");

			Set<Class<? extends Agent>> agentClasses = reflections.getSubTypesOf(Agent.class);
			agentClasses.stream().sorted(Comparator.comparing(Class::getSimpleName)).forEach((cls) -> {
				try {
					Constructor<?> constructor = cls.getConstructor(AgentConfiguration.class);
					var agentHolder = new AgentSupplier((config) -> {
						try {
							return (Agent) constructor.newInstance(config);
						} catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
							e.printStackTrace();
						}
						return null;
					}, cls.getSimpleName());
					Application.AGENT_SUPPLIERS.add(agentHolder);
				} catch (NoSuchMethodException e) {
					e.printStackTrace();
				}
			});
		}
	}

	private static void loadHeuristics() {
		{
			Reflections reflections = new Reflections("net.rahka.chess.agent.heuristics");

			Set<Class<? extends Heuristic>> heuristicClasses = reflections.getSubTypesOf(Heuristic.class);
			heuristicClasses.stream().sorted(Comparator.comparing(Class::getSimpleName)).forEach((cls) -> {
				try {
					Constructor<?> constructor = cls.getConstructor((Class<?>[]) null);
					var heuristicHolder = new HeuristicSupplier(cls.getSimpleName(), () -> {
						try {
							return (Heuristic) constructor.newInstance();
						} catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
							e.printStackTrace();
						}
						return null;
					});
					Application.HEURISTIC_SUPPLIERS.add(heuristicHolder);
				} catch (NoSuchMethodException e) {
					e.printStackTrace();
				}
			});
		}
	}

	public class HumanAgent implements Agent {

		private Move previousMove;

		@Override
		public Move getMove(Player player, Iterator<Move> moves, State state) {
			Set<Move> validMoves = new HashSet<>();
			moves.forEachRemaining(validMoves::add);

			getVisualizer().setCanSelectBlackPieces(player.isBlack());
			getVisualizer().setCanSelectWhitePieces(player.isWhite());

			do {
				pendingMove.clear();
				synchronized (pendingMove) {
					try {
						pendingMove.wait();
					} catch (InterruptedException e) {
						System.out.println("Pending move interrupted, waiting anew!");
					}
				}
			} while (!pendingMove.isPresent() || validMoves.stream().noneMatch(move -> matches(pendingMove.get(), move)));

			previousMove = pendingMove.get();
			return pendingMove.get();
		}

		private boolean matches(Move m1, Move m2) {
			return (m1.move == m2.move && m1.piece == m2.piece);
		}

		@Override
		public void postMove(Move move) {
			if (move == previousMove) {
				getVisualizer().nextMove();
			}
		}

	}

	private class OptionalMove {

		private Move move;

		void clear() {move = null;}

		void set(Move move) {this.move = move;}

		Move get() {return this.move;}

		boolean isPresent() {return (this.move != null);}

	}

}
