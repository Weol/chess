package net.rahka;

import com.sun.javafx.css.StyleManager;
import javafx.scene.Scene;
import javafx.stage.Stage;
import net.rahka.agent.RandomAgent;
import net.rahka.agent.RandomKillingAgent;
import net.rahka.agent.RandomPriorityKillingAgent;
import net.rahka.chess.BoardConfig;
import net.rahka.chess.Chess;
import net.rahka.chess.Player;
import net.rahka.visualizer.Visualizer;

public class Application extends javafx.application.Application {

	public static void main(String[] args) {
		Application.launch();
	}

	private Chess chess;

	@Override
	public void init() throws Exception {
		super.init();

		var whiteAgent = new RandomPriorityKillingAgent();
		var blackAGent = new RandomPriorityKillingAgent();

		chess = new Chess(BoardConfig.DEFAULT, whiteAgent, blackAGent);
	}

	@Override
	public void start(Stage stage) throws Exception {
		javafx.application.Application.setUserAgentStylesheet(javafx.application.Application.STYLESHEET_MODENA);
		StyleManager.getInstance().addUserAgentStylesheet("application.css");

		stage.setTitle("Visualizer");
		stage.setMinWidth(700);
		stage.setMinHeight(700);
		stage.setWidth(700);
		stage.setHeight(700);

		var agentClasses = new AgentHolder[] {
			new AgentHolder<>(RandomAgent.class, "Random"),
			new AgentHolder<>(RandomKillingAgent.class, "Random killing"),
			new AgentHolder<>(RandomKillingAgent.class, "Random priority killing"),
		};

		var visualizer = new Visualizer(BoardConfig.DEFAULT, agentClasses) {

			@Override
			protected void execute() {
				System.out.println("Starting chess match!");
				Player winner = chess.start();
				System.out.printf("Chess match finished! %s won!\n", winner.toString());
			}

		};
		chess.addOnMoveHandler(visualizer::onMove);

		stage.setScene(new Scene(visualizer));
		stage.show();
	}

}
