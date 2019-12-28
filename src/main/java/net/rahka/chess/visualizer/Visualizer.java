package net.rahka.chess.visualizer;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import lombok.Getter;
import lombok.Setter;
import net.rahka.chess.AgentHolder;
import net.rahka.chess.IO;
import net.rahka.chess.game.Board;
import net.rahka.chess.game.Move;
import net.rahka.chess.game.Piece;
import net.rahka.chess.game.Player;
import net.rahka.chess.utils.AdjustableTimer;
import net.rahka.chess.utils.StateCursorList;

import java.util.Iterator;

public abstract class Visualizer extends Pane {

	private static int PIECE_PADDING = 10;

	private static IO.Images getPieceImage(Piece piece) {
		switch (piece) {
			case WHITE_PAWN:
				return IO.Images.WHITE_PAWN;
			case WHITE_ROOK:
				return IO.Images.WHITE_ROOK;
			case WHITE_KNIGHT:
				return IO.Images.WHITE_KNIGHT;
			case WHITE_BISHOP:
				return IO.Images.WHITE_BISHOP;
			case WHITE_QUEEN:
				return IO.Images.WHITE_QUEEN;
			case WHITE_KING:
				return IO.Images.WHITE_KING;
			case BLACK_PAWN:
				return IO.Images.BLACK_PAWN;
			case BLACK_ROOK:
				return IO.Images.BLACK_ROOK;
			case BLACK_KNIGHT:
				return IO.Images.BLACK_KNIGHT;
			case BLACK_BISHOP:
				return IO.Images.BLACK_BISHOP;
			case BLACK_QUEEN:
				return IO.Images.BLACK_QUEEN;
			case BLACK_KING:
				return IO.Images.BLACK_KING;
		}
		return IO.Images.BLACK_ROOK;
	}

	//Views
	private final ChessBoard boardView;
	private final PlayBackView playBackView;

	private final ChessPieceView[] imageViews;
	private final StateCursorList states;
	private final long[] initialState;

	//State
	private Thread executionThread;

	//Concurrency
	private AdjustableTimer moveTimer = new AdjustableTimer();

	public Visualizer(AgentHolder[] agentClasses, long[] initialState) {
		this.initialState = initialState;
		states = new StateCursorList();
		imageViews = new ChessPieceView[8*4];

		playBackView = new PlayBackView(agentClasses);
		playBackView.prefWidthProperty().bind(widthProperty());
		playBackView.setPrefHeight(30);

		boardView = new ChessBoard();
		boardView.setOnMouseClicked((e) -> {
			int x = (int) e.getX(), y = (int) e.getY();
			int size = (int) boardView.getWidth() / 8;

			onChessPieceClicked(null, x / size, y / size);
		});
		boardView.layoutYProperty().bind(playBackView.heightProperty());
		boardView.layoutXProperty().bind(widthProperty().divide(2).subtract(boardView.widthProperty().divide(2)));
		boardView.setWidth(Math.min(getWidth(), getHeight() - playBackView.getHeight()));
		boardView.setHeight(Math.min(getWidth(), getHeight() - playBackView.getHeight()));

		//Group that holds all the chess piece ImageViews
		Group piecesGroup = new Group();
		piecesGroup.setManaged(false);
		piecesGroup.translateXProperty().bind(boardView.layoutXProperty().add(PIECE_PADDING));
		piecesGroup.translateYProperty().bind(boardView.layoutYProperty().add(PIECE_PADDING));

		for (int i = 0; i < imageViews.length; i++) {
			imageViews[i] = new ChessPieceView();
			final var view = imageViews[i];
			view.setOnMouseClicked((ignored) -> onChessPieceClicked(view.getPiece(), view.getBoardX(), view.getBoardY()));
			piecesGroup.getChildren().add(view);
		}

		getChildren().add(playBackView);
		getChildren().add(boardView);
		getChildren().add(piecesGroup);

		widthProperty().addListener((ignored) -> resetPieces());
		heightProperty().addListener((ignored) -> resetPieces());

		reset(true);
	}

	private void onChessPieceClicked(Piece piece, int x, int y) {
		if (boardView.isSquareHighlighted(x, y)) {
			boardView.unHighlightAll();
			return;
		}

		long kernel = Board.kernelOf(x, y);
		Board board = new Board(states.cursor().current());

		if ((kernel & board.getAllPieces()) != 0) {
			for (ChessPieceView imageView : imageViews) {
				if (imageView.getBoardX() == x && imageView.getBoardY() == y) {
					piece = imageView.getPiece();
				}
			}
		}

		long movable = 0;
		if (piece == null) {
			Iterator<Move> blackMoves = board.getAllLegalMoves(Player.BLACK);
			Iterator<Move> whiteMoves = board.getAllLegalMoves(Player.WHITE);

			while (blackMoves.hasNext() || whiteMoves.hasNext()) {
				Move move = (blackMoves.hasNext()) ? blackMoves.next() : whiteMoves.next();

				if ((kernel & move.move) != 0) {
					movable |= move.move;
				}
			}
			movable ^= kernel;
		}

		long attackers = 0;
		long victims = 0;
		if (piece != null) {
			{
				Iterator<Move> moves = board.getAllLegalMoves(piece.getPlayer().not());
				while (moves.hasNext()) {
					Move move = moves.next();

					if ((kernel & move.move) != 0) {
						attackers |= move.move;
					}
				}
				attackers ^= kernel;
			}

			{
				Iterator<Move> moves = board.getAllLegalMoves(piece, x, y);
				while (moves.hasNext()) {
					Move move = moves.next();

					if ((kernel & move.move) != 0) {
						victims |= move.move;
					}
				}
				victims ^= kernel;
			}
		}

		boardView.unHighlightAll();
		boardView.highlightSquare(x, y, Color.GREEN);
		for (int i = 0; i < 64; i++) {
			long k = (-0x8000000000000000L >>> (63 - i));

			if (i == y * 8 + x) continue;
			if ((k & victims) != 0 && (k & attackers) != 0) {
				boardView.highlightSquare(i % 8, i / 8, Color.YELLOW);
			} else if ((k & victims) != 0) {
				boardView.highlightSquare(i % 8, i / 8, Color.BLUE);
			} else if ((k & attackers) != 0 || (k & movable) != 0) {
				boardView.highlightSquare(i % 8, i / 8, Color.RED);
			}
		}
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

				if (!states.cursor().hasNext() && !executionThread.isAlive()) {
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

		for (ChessPieceView view : imageViews) {
			view.setFitWidth(size / 8 - PIECE_PADDING * 2);
			view.setFitHeight(size / 8 - PIECE_PADDING * 2);
			view.setX((view.getFitWidth() + PIECE_PADDING * 2) * view.getBoardX());
			view.setY((view.getFitHeight() + PIECE_PADDING * 2) * view.getBoardY());
		}

		boardView.paint();
	}

	public void reset(boolean hard) {
		synchronized (this) {
			if (hard) {
				states.clear();
				states.push(initialState);
				changeState(states.cursor().current());
				playBackView.resetButton.setVisible(false);
			} else {
				states.cursor().reset();
				changeState(states.cursor().current());
			}

			resetPieces();
		}
	}

	private void previousMove() {
		synchronized (this) {
			if (states.cursor().hasPrevious()) {
				changeState(states.cursor().previous());
			}
		}
	}

	private void nextMove() {
		synchronized (this) {
			if (states.cursor().hasNext()) {
				changeState(states.cursor().next());
			}
		}
	}

	private void changeState(long[] state) {
		boardView.unHighlightAll();
		for (ChessPieceView imageView : imageViews) {
			imageView.setVisible(false);
		}

		int index = 0;
		for (Piece piece : Piece.values()) {
			long pieces = state[piece.index];
			for (int i = 0; i < 64; i++) {
				long kernel = (-0x8000000000000000L >>> (63 - i));
				if ((pieces & kernel) != 0) {
					var view = imageViews[index++];
					view.setVisible(true);
					view.setPiece(piece);
					view.setBoardX(i % 8);
					view.setBoardY(i / 8);
				}
			}
		}

		resetPieces();
	}

	public synchronized void onBoardStateChanged(long[] state) {
		states.push(state);
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
		if (executionThread != null && executionThread.isAlive()) {
			pause();
			executionThread.interrupt();
			executionThread = null;
			reset(true);
			playBackView.startStopButton.setImage(IO.image(IO.Images.EXECUTE));
		} else if (states.size() > 1) {
			pause();
			reset(true);
			playBackView.startStopButton.setImage(IO.image(IO.Images.EXECUTE));
		} else {
			reset(true);
			start();
		}
	}

	private static class ChessPieceView extends ImageView {

		@Getter @Setter
		private int boardX, boardY;

		private Piece piece;

		public Piece getPiece() {
			return piece;
		}

		public void setPiece(Piece piece) {
			this.piece = piece;
			setImage(IO.image(getPieceImage(piece)));
			setSmooth(true);
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
