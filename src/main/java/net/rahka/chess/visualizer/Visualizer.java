package net.rahka.chess.visualizer;

import javafx.animation.AnimationTimer;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.rahka.chess.AgentHolder;
import net.rahka.chess.IO;
import net.rahka.chess.BoardConfig;
import net.rahka.chess.game.Player;
import net.rahka.chess.game.pieces.Bishop;
import net.rahka.chess.game.pieces.King;
import net.rahka.chess.game.pieces.Knight;
import net.rahka.chess.game.pieces.Move;
import net.rahka.chess.game.pieces.Pawn;
import net.rahka.chess.game.pieces.Piece;
import net.rahka.chess.game.pieces.Queen;
import net.rahka.chess.game.pieces.Rook;
import net.rahka.chess.utils.AdjustableTimer;
import net.rahka.chess.utils.CursorList;

import java.util.*;
import java.util.concurrent.*;

public abstract class Visualizer extends Pane {

	private static int PIECE_PADDING = 10;

	private static IO.Images getPieceImage(Piece piece) {
		if (piece.getPlayer() == Player.WHITE) {
			if (piece.getClass().isAssignableFrom(Pawn.class)) {
				return IO.Images.WHITE_PAWN;
			} else if (piece.getClass().isAssignableFrom(Bishop.class)) {
				return IO.Images.WHITE_BISHOP;
			} else if (piece.getClass().isAssignableFrom(King.class)) {
				return IO.Images.WHITE_KING;
			} else if (piece.getClass().isAssignableFrom(Queen.class)) {
				return IO.Images.WHITE_QUEEN;
			} else if (piece.getClass().isAssignableFrom(Knight.class)) {
				return IO.Images.WHITE_KNIGHT;
			} else if (piece.getClass().isAssignableFrom(Rook.class)) {
				return IO.Images.WHITE_ROOK;
			}
		} else {
			if (piece.getClass().isAssignableFrom(Pawn.class)) {
				return IO.Images.BLACK_PAWN;
			} else if (piece.getClass().isAssignableFrom(Bishop.class)) {
				return IO.Images.BLACK_BISHOP;
			} else if (piece.getClass().isAssignableFrom(King.class)) {
				return IO.Images.BLACK_KING;
			} else if (piece.getClass().isAssignableFrom(Queen.class)) {
				return IO.Images.BLACK_QUEEN;
			} else if (piece.getClass().isAssignableFrom(Knight.class)) {
				return IO.Images.BLACK_KNIGHT;
			} else if (piece.getClass().isAssignableFrom(Rook.class)) {
				return IO.Images.BLACK_ROOK;
			}
		}
		return IO.Images.BLACK_ROOK;
	}

	//Views
	private final ChessBoard boardView;
	private final PlayBackView playBackView;
	private final Group piecesGroup;

	private final BoardConfig boardConfiguration;
	private final BlockingQueue<Animation> animations;
	private final Map<Piece, ChessPieceView> imageViews;
	private final CursorList<Move> moves;

	//State
	private Thread executionThread;

	//Concurrency
	private AdjustableTimer moveTimer = new AdjustableTimer();

	public Visualizer(BoardConfig config, AgentHolder[] agentClasses) {
		boardConfiguration = config;
		animations = new LinkedBlockingQueue<>(1);
		moves = new CursorList<>();
		imageViews = new HashMap<>();

		playBackView = new PlayBackView(agentClasses);
		playBackView.prefWidthProperty().bind(widthProperty());
		playBackView.setPrefHeight(30);

		boardView = new ChessBoard(config.getDimensions());
		boardView.layoutYProperty().bind(playBackView.heightProperty());
		boardView.layoutXProperty().bind(widthProperty().divide(2).subtract(boardView.widthProperty().divide(2)));
		boardView.setWidth(Math.min(getWidth(), getHeight() - playBackView.getHeight()));
		boardView.setHeight(Math.min(getWidth(), getHeight() - playBackView.getHeight()));

		//Group that holds all the chess piece ImageViews
		piecesGroup = new Group();
		piecesGroup.setManaged(false);
		piecesGroup.translateXProperty().bind(boardView.layoutXProperty().add(PIECE_PADDING));
		piecesGroup.translateYProperty().bind(boardView.layoutYProperty().add(PIECE_PADDING));

		getChildren().add(playBackView);
		getChildren().add(boardView);
		getChildren().add(piecesGroup);

		widthProperty().addListener((ignored) -> resetPieces());
		heightProperty().addListener((ignored) -> resetPieces());

		reset(true);

		resetPieces();

		var animator = new AnimationTimer() {
			@Override
			public void handle(long now) {
				Visualizer.this.animate();
			}
		};
		animator.start();
	}

	private long getMoveInterval() {
		return 1000 - (long) playBackView.animationRateSlider.getValue();
	}

	private void onMoveIntervalChanged() {
		moveTimer.adjust(getMoveInterval());
	}

	private void play() {
		if (!moveTimer.isRunning()) {
			moveTimer.start(() -> {
				nextMove();

				if (!moves.cursor().hasNext() && !executionThread.isAlive()) {
					pause();
				}
			}, getMoveInterval());
		}
		playBackView.playPauseButton.setImage(IO.image(IO.Images.PAUSE));
	}

	private void pause() {
		if (moveTimer.isRunning()) {
			moveTimer.shutdown();
		}
		playBackView.playPauseButton.setImage(IO.image(IO.Images.PLAY));
	}

	private void start() {
		executionThread = new Thread(() -> {
			playBackView.startStopButton.setImage(IO.image(IO.Images.STOP));
			execute();
			playBackView.startStopButton.setImage(IO.image(IO.Images.STOP));
			playBackView.resetButton.setVisible(true);
		});

		executionThread.start();
	}

	protected abstract void execute();

	protected abstract void onWhiteAgentChosen(AgentHolder agentHolder);

	protected abstract void onBlackAgentChosen(AgentHolder agentHolder);


	private void resetPieces() {
		var size = Math.min(getWidth(), getHeight() - playBackView.getHeight());
		boardView.setWidth(size);
		boardView.setHeight(size);

		for (Map.Entry<Piece, ChessPieceView> entry : imageViews.entrySet()) {
			var view = entry.getValue();

			view.setFitWidth(size / boardConfiguration.getDimensions() - PIECE_PADDING * 2);
			view.setFitHeight(size / boardConfiguration.getDimensions() - PIECE_PADDING * 2);
			view.setX((view.getFitWidth() + PIECE_PADDING * 2) * view.getBoardX());
			view.setY((view.getFitHeight() + PIECE_PADDING * 2) * view.getBoardY());
		}

		boardView.paint();
	}

	public synchronized void reset(boolean hard) {
		if (hard) {
			moves.clear();
			playBackView.resetButton.setVisible(false);
		} else {
			moves.cursor().reset();
		}

		animations.clear();
		piecesGroup.getChildren().clear();
		imageViews.clear();

		var pieces = boardConfiguration.configurations();
		while (pieces.hasNext()) {
			var pieceConfig = pieces.next();
			var piece = pieceConfig.getPiece();
			var view = new ChessPieceView(IO.image(getPieceImage(piece)), pieceConfig.getStartX(), pieceConfig.getStartY());

			imageViews.put(piece, view);

			piecesGroup.getChildren().add(view);
		}

		resetPieces();
	}

	private  void animate() {
		var iterator = animations.iterator();
		while (iterator.hasNext()) {
			var animation = iterator.next();
			var move = animation.move;
			var from = imageViews.get(animation.move.piece);

			var toX = (from.getFitWidth() + PIECE_PADDING * 2) * (animation.reverse ? move.fromX : move.toX);
			var toY = (from.getFitHeight() + PIECE_PADDING * 2) * (animation.reverse ? move.fromY : move.toY);

			var stepSize = Math.min(boardView.getWidth(), boardView.getHeight()) / Math.max(getMoveInterval(), 50);

			var deltaX = Math.min(Math.abs(toX - from.getX()), stepSize) * Math.signum(toX - from.getX());
			var deltaY = Math.min(Math.abs(toY - from.getY()), stepSize) * Math.signum(toY - from.getY());

			from.setX(from.getX() + deltaX);
			from.setY(from.getY() + deltaY);

			if (Math.abs(toX - from.getX()) < 0.1 && Math.abs(toY - from.getY()) < 0.1) {
				iterator.remove();

				if (move.victim != null) {
					imageViews.get(animation.move.victim).setVisible(animation.reverse);
				}
			}
		}
	}

	private void previousMove() {
		if (moves.cursor().hasPrevious() && animations.remainingCapacity() > 0) {
			var move = moves.cursor().current();
			var from = imageViews.get(move.piece);

			from.boardX = move.fromX;
			from.boardY = move.fromY;

			animations.add(new Animation(move, true));

			moves.cursor().previous();
		}
	}

	private synchronized void nextMove() {
		if (moves.cursor().hasNext() && animations.remainingCapacity() > 0) {
			Move move = moves.cursor().next();

			final var from = imageViews.get(move.piece);

			from.boardX = move.toX;
			from.boardY = move.toY;

			animations.add(new Animation(move));
		}
	}

	public synchronized void onMove(Move move) {
		moves.push(move);
	}

	private void onPreviousButtonPressed() {
		previousMove();
	}

	private void onNextButtonPressed() {
		nextMove();
	}

	private void onResetButtonPressed() {
		reset(false);
	}

	private void onPlayPauseButtonPressed() {
		if (moveTimer.isRunning()) {
			pause();
		} else {
			play();
		}
	}

	private void onStartStopButtonPressed() {
		pause();

		if (executionThread != null && executionThread.isAlive()) {
			executionThread.interrupt();
			executionThread = null;
			reset(true);
			playBackView.startStopButton.setImage(IO.image(IO.Images.EXECUTE));
		} else if (moves.size() > 0) {
			reset(true);
			playBackView.startStopButton.setImage(IO.image(IO.Images.EXECUTE));
		} else {
			reset(true);
			start();
		}
	}

	@AllArgsConstructor
	@RequiredArgsConstructor
	private static class Animation {

		@NonNull @Getter
		private Move move;

		private boolean reverse = false;

	}

	private static class ChessPieceView extends ImageView {

		@NonNull @Getter
		private int boardX, boardY;

		ChessPieceView(Image image, int x, int y) {
			super(image);
			boardX = x;
			boardY = y;
		}

	}

	private class PlayBackView extends BorderPane {

		private static final int CHILDREN_HEIGHT = 17;

		private ImageButton playPauseButton, startStopButton, resetButton;

		private Slider animationRateSlider;

		public PlayBackView(AgentHolder[] agentClasses) {
			getStyleClass().add("playback-controls");

			setPadding(new Insets(5, 10, 5, 10));

			ImageButton previousButton = new ImageButton(IO.image(IO.Images.PREVIOUS));
			previousButton.getStyleClass().add("playback-button");
			previousButton.setPrefHeight(CHILDREN_HEIGHT);
			previousButton.setOnAction((ignored) -> onPreviousButtonPressed());

			ImageButton nextButton = new ImageButton(IO.image(IO.Images.NEXT));
			nextButton.getStyleClass().add("playback-button");
			nextButton.setPrefHeight(CHILDREN_HEIGHT);
			nextButton.setOnAction((ignored) -> onNextButtonPressed());

			playPauseButton = new ImageButton(IO.image(IO.Images.PLAY));
			playPauseButton.getStyleClass().add("playback-button");
			playPauseButton.setPrefHeight(CHILDREN_HEIGHT);
			playPauseButton.setOnAction((ignored) -> onPlayPauseButtonPressed());

			startStopButton = new ImageButton(IO.image(IO.Images.EXECUTE));
			startStopButton.getStyleClass().add("playback-button");
			startStopButton.setPrefHeight(CHILDREN_HEIGHT);
			startStopButton.setOnAction((ignored) -> onStartStopButtonPressed());

			resetButton = new ImageButton(IO.image(IO.Images.RESET));
			resetButton.getStyleClass().add("playback-button");
			resetButton.setPrefHeight(CHILDREN_HEIGHT);
			resetButton.setOnAction((ignored) -> onResetButtonPressed());
			resetButton.setVisible(false);

			var classesList = FXCollections.observableArrayList(agentClasses);
			var whiteAgentComboBox = new ComboBox<>(classesList);
			whiteAgentComboBox.valueProperty().set(agentClasses[0]);
			whiteAgentComboBox.valueProperty().addListener((property, old, now) -> {
				onWhiteAgentChosen(now);
			});

			var blackAgentComboBox = new ComboBox<>(classesList);
			blackAgentComboBox.valueProperty().set(agentClasses[0]);
			blackAgentComboBox.valueProperty().addListener((property, old, now) -> {
				onBlackAgentChosen(now);
			});

			var whiteLabel = new Label("White:");
			var blackLabel = new Label("Black:");

			whiteLabel.setLabelFor(whiteAgentComboBox);
			blackLabel.setLabelFor(blackAgentComboBox);

			whiteLabel.setMinWidth(40);

			var gridPane = new GridPane();
			gridPane.setHgap(10);
			gridPane.setVgap(10);
			gridPane.setPadding(new Insets(10, 10, 10, 10));
			gridPane.add(whiteLabel, 0, 0);
			gridPane.add(whiteAgentComboBox, 1, 0);
			gridPane.add(blackLabel, 0, 1);
			gridPane.add(blackAgentComboBox, 1, 1);

			var agentsButton = new DropdownPopupButton("Agents", gridPane);
			agentsButton.setPrefHeight(CHILDREN_HEIGHT);
			agentsButton.setContentDisplay(ContentDisplay.RIGHT);

			animationRateSlider = new Slider();
			animationRateSlider.setMin(10);
			animationRateSlider.setMax(1000);
			animationRateSlider.setValue(800);
			animationRateSlider.setShowTickLabels(false);
			animationRateSlider.setShowTickMarks(false);
			animationRateSlider.setMajorTickUnit(10);
			animationRateSlider.setMinorTickCount(5);
			animationRateSlider.setBlockIncrement(10);
			animationRateSlider.setPrefWidth(60);
			animationRateSlider.valueProperty().addListener((ignored) -> {
				onMoveIntervalChanged();
			});

			var leftHBox = new HBox(10);
			leftHBox.setAlignment(Pos.CENTER_LEFT);
			leftHBox.getChildren().add(agentsButton);
			leftHBox.getChildren().add(animationRateSlider);

			var middleHBox = new HBox(10);
			middleHBox.setAlignment(Pos.CENTER);
			middleHBox.getChildren().add(previousButton);
			middleHBox.getChildren().add(playPauseButton);
			middleHBox.getChildren().add(nextButton);

			var rightHBox = new HBox(10);
			rightHBox.setAlignment(Pos.CENTER_RIGHT);
			rightHBox.getChildren().add(resetButton);
			rightHBox.getChildren().add(startStopButton);
			rightHBox.prefWidthProperty().bind(leftHBox.widthProperty());

			setLeft(leftHBox);
			setCenter(middleHBox);
			setRight(rightHBox);
		}

	}


}
