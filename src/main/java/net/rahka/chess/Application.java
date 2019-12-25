package net.rahka.chess;

import com.sun.javafx.css.StyleManager;
import javafx.scene.Scene;
import javafx.stage.Stage;
import net.rahka.chess.agent.*;
import net.rahka.chess.game.Chess;
import net.rahka.chess.game.Player;
import net.rahka.paramaters.FunctionFlag;
import net.rahka.paramaters.CollectionFlag;
import net.rahka.paramaters.ParameterInterpreter;
import net.rahka.chess.visualizer.Visualizer;

import java.util.Arrays;
import java.util.HashMap;
import java.util.function.Supplier;


public class Application extends javafx.application.Application {

	public static void main(String[] args) {

		if (args.length > 0) {
			var agents = new HashMap<String, Supplier<Agent>>();
			agents.put("Random", RandomAgent::new);
			agents.put("RandomKilling", RandomKillingAgent::new);
			agents.put("RandomPriorityKilling", RandomPriorityKillingAgent::new);
			agents.put("SimpleHeuristic", GreedyHeuristicAgent::new);
			agents.put("MiniMax", MiniMaxAgent::new);

			ParameterInterpreter interpreter = new ParameterInterpreter(
					new CollectionFlag("black", "b", "The agent of the black player", agents.keySet()),
					new CollectionFlag("white", "w", "The agent of the white player", agents.keySet()),
					new FunctionFlag("number", "n", "Number of games to play", Integer::valueOf)
			);
			var interpretation = interpreter.intepret(args);

			var blackAgent = agents.get((String) interpretation.get("black"));
			var whiteAgent = agents.get((String) interpretation.get("white"));
			int games = interpretation.get("number");

			CLI.run(blackAgent.get(), whiteAgent.get(), games);
		} else {
			Application.launch();
		}
	}

	private Chess chess;

	@Override
	public void init() throws Exception {
		super.init();

		var whiteAgent = new MiniMaxAgent();
		var blackAGent = new MiniMaxAgent();

		chess = new Chess(whiteAgent, blackAGent);
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

		var agentClasses = new AgentHolder[] {
			new AgentHolder(RandomPriorityKillingAgent::new, "Random priority killing"),
			new AgentHolder(RandomAgent::new, "Random"),
			new AgentHolder(RandomKillingAgent::new, "Random killing"),
			new AgentHolder(GreedyHeuristicAgent::new, "Greedy heuristic"),
			new AgentHolder(MiniMaxAgent::new, "MiniMax"),
		};

		var visualizer = new Visualizer(agentClasses, Arrays.copyOf(chess.getBoard().getState(), 12)) {

			@Override
			protected void execute() {
				System.out.println("Starting chess match!");
				Player winner = chess.start();
				if (winner != null) {
					System.out.printf("Chess match finished! %s won!\n", winner.toString());
				} else {
					System.out.println("Chess match interrupted!");
				}
			}

			@Override
			protected void onWhiteAgentChosen(AgentHolder agentHolder) {
				chess.setWhiteAgent(agentHolder.getSupplier().get());
			}

			@Override
			protected void onBlackAgentChosen(AgentHolder agentHolder) {
				chess.setBlackAgent(agentHolder.getSupplier().get());
			}

		};
		chess.setBoardChangeHandler(visualizer::onBoardStateChanged);

		stage.setScene(new Scene(visualizer));
		stage.show();
	}

}
