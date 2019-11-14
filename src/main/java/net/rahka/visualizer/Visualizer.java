package net.rahka.visualizer;

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
import javafx.util.converter.IntegerStringConverter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.rahka.AgentHolder;
import net.rahka.IO;
import net.rahka.chess.BoardConfig;
import net.rahka.chess.Player;
import net.rahka.chess.pieces.Bishop;
import net.rahka.chess.pieces.King;
import net.rahka.chess.pieces.Knight;
import net.rahka.chess.pieces.Move;
import net.rahka.chess.pieces.Pawn;
import net.rahka.chess.pieces.Piece;
import net.rahka.chess.pieces.Queen;
import net.rahka.chess.pieces.Rook;

import javax.swing.Timer;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.UnaryOperator;

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
	private final Stack<Runnable> previousMoves;
	private final HashQueueMap<ImageView, Animation> animations;
	private final Map<Piece, ChessPieceView> imageViews;
	private final CursorList<Move> moves;

	//State
	private ScheduledFuture<?> playingFuture;
	private Thread executionThread;

	//Concurrency
	private ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

	public Visualizer(BoardConfig config, AgentHolder<?>[] agentClasses) {
		boardConfiguration = config;
		animations = new HashQueueMap<>();
		moves = new CursorList<>();
		imageViews = new HashMap<>();
		previousMoves = new Stack<>();

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

		reset();

		resetPieces();

		var animator = new AnimationTimer() {
			@Override
			public void handle(long now) {
				Visualizer.this.animate();
			}
		};
		animator.start();
	}

	private boolean isPlaying() {
		return (playingFuture != null);
	}

	private boolean hasExecuted() {
		return (executionThread != null);
	}

	private synchronized void play() {
		if (!isPlaying()) {
			playingFuture = scheduler.scheduleWithFixedDelay(this::nextMove,
			(long) playBackView.animationRateSlider.getValue(), (long) playBackView.animationRateSlider.getValue(), TimeUnit.MILLISECONDS);
		}
		playBackView.playPauseButton.setImage(IO.image(IO.Images.PAUSE));
	}

	private synchronized void pause() {
		if (isPlaying()) {
			playingFuture.cancel(false);
		}
		playingFuture = null;
		playBackView.playPauseButton.setImage(IO.image(IO.Images.PLAY));
	}

	private void start() {
		executionThread = new Thread(() -> {
			playBackView.startStopButton.setImage(IO.image(IO.Images.STOP));
			execute();
			playBackView.startStopButton.setImage(IO.image(IO.Images.RESET));
		});

		executionThread.start();
	}

	protected abstract void execute();

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

	public void reset() {
		synchronized (this) {
			moves.clear();
			animations.clear();
			piecesGroup.getChildren().clear();
			imageViews.clear();
			previousMoves.clear();

			var pieces = boardConfiguration.configurations();
			while (pieces.hasNext()) {
				var pieceConfig = pieces.next();
				var piece = pieceConfig.getPiece();
				var view = new ChessPieceView(IO.image(getPieceImage(piece)), pieceConfig.getStartX(), pieceConfig.getStartY());

				imageViews.put(piece, view);

				piecesGroup.getChildren().add(view);
			}

			resetPieces();

			this.notifyAll();
		}
	}

	private void nextMove() {
		if (moves.cursor().hasNext()) {
			Move nextMove = moves.cursor().next();
			performMove(nextMove);
		}
	}

	private void previousMove() {
		if (!previousMoves.isEmpty()) previousMoves.pop().run();
	}

	private void animate() {
		synchronized (animations) {
			var iterator = animations.headIterator();
			while (iterator.hasNext()) {
				var animation = iterator.next();
				var view = animation.view;

				if (Math.abs(animation.toX - view.getX()) > 0.01 || Math.abs(animation.toY - view.getY()) > 0.01) {
					var deltaX = (animation.toX - view.getX());
					var deltaY = (animation.toY - view.getY());

					view.setX(view.getX() + deltaX * 0.1);
					view.setY(view.getY() + deltaY * 0.1);

					if (Math.abs(animation.toX - view.getX()) < 10 && Math.abs(animation.toY - view.getY()) < 10)
						if (animation.after != null) animation.after.run();
				} else {
					iterator.remove();
				}
			}
		}
	}

	private void performMove(final Move move) {
		final var from = imageViews.get(move.piece);
		final var to = imageViews.get(move.victim);

		from.boardX = move.toX;
		from.boardY = move.toY;

		final var toX = Math.max(0, (from.getFitWidth() + PIECE_PADDING * 2) * move.toX);
		final var toY = Math.max(0, (from.getFitHeight() + PIECE_PADDING * 2) * move.toY);

		if (to != null) {
			var animation = new Animation(from, toX, toY, () -> to.setVisible(false));
			synchronized (animations) {
				animations.push(from, animation);
			}
		} else {
			var animation = new Animation(from, toX, toY);
			synchronized (animations) {
				animations.push(from, animation);
			}
		}

		previousMoves.push(() -> {
			if (to != null) {
				to.setVisible(true);
			}

			moves.cursor().previous();

			var animation = new Animation(from, Math.max(0, (from.getFitWidth() + PIECE_PADDING * 2) * move.fromX), Math.max(0, (from.getFitHeight() + PIECE_PADDING * 2) * move.fromY));

			synchronized (animations) {animations.push(from, animation);}
		});
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

	private void onPlayPauseButtonPressed() {
		if (isPlaying()) {
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
			reset();
			playBackView.startStopButton.setImage(IO.image(IO.Images.EXECUTE));
		} else if (moves.size() > 0) {
			reset();
			playBackView.startStopButton.setImage(IO.image(IO.Images.EXECUTE));
		} else {
			reset();
			start();
		}
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

	@RequiredArgsConstructor
	@AllArgsConstructor
	private static class Animation {

		@NonNull
		ImageView view;

		@NonNull
		double toX, toY;

		Runnable after;

		@Override
		public int hashCode() {
			return view.hashCode();
		}

	}

	private class PlayBackView extends BorderPane {

		private static final int CHILDREN_HEIGHT = 17;

		private ImageButton playPauseButton, startStopButton;
		private Slider animationRateSlider;

		public PlayBackView(AgentHolder<?>[] agentClasses) {
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

			var classesList = FXCollections.observableArrayList(agentClasses);
			var whiteAgentComboBox = new ComboBox<>(classesList);
			var blackAgentComboBox = new ComboBox<>(classesList);
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
			animationRateSlider.setValue(500);
			animationRateSlider.setShowTickLabels(false);
			animationRateSlider.setShowTickMarks(false);
			animationRateSlider.setMajorTickUnit(10);
			animationRateSlider.setMinorTickCount(5);
			animationRateSlider.setBlockIncrement(10);
			animationRateSlider.setPrefWidth(60);

			var leftHBox = new HBox(10);
			leftHBox.setAlignment(Pos.CENTER);
			leftHBox.getChildren().add(agentsButton);
			leftHBox.getChildren().add(animationRateSlider);

			var middleHBox = new HBox(10);
			middleHBox.setAlignment(Pos.CENTER);
			middleHBox.getChildren().add(previousButton);
			middleHBox.getChildren().add(playPauseButton);
			middleHBox.getChildren().add(nextButton);

			setLeft(leftHBox);
			setCenter(middleHBox);
			setRight(startStopButton);

			setAlignment(agentsButton,Pos.CENTER);
			setAlignment(startStopButton,Pos.CENTER);
		}

	}


}
