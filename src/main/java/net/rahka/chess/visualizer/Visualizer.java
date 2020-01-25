package net.rahka.chess.visualizer;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import lombok.Getter;
import lombok.NonNull;
import net.rahka.chess.AgentSupplier;
import net.rahka.chess.HeuristicSupplier;
import net.rahka.chess.IO;
import net.rahka.chess.agent.AgentConfiguration;
import net.rahka.chess.game.Board;
import net.rahka.chess.game.Match;
import net.rahka.chess.game.Piece;
import net.rahka.chess.game.Player;
import net.rahka.chess.utils.AdjustableTimer;

import java.util.Arrays;
import java.util.function.Supplier;

public class Visualizer extends Pane {

	private static final int CONTEXT_MENU_HIDE_PADDING = 30;

	private final ChessBoard boardView;
	private final PlayBackView playBackView;
	private final EditContextMenu menu;

	private final AdjustableTimer moveTimer;

	private int moveIndex;

	@NonNull @Getter
	private Match match;

	@Getter
	private Supplier<Match> matchSupplier;

	@NonNull @Getter
	private final ObservableList<AgentSupplier> agentSuppliers = FXCollections.observableArrayList();

	@NonNull @Getter
	private final ObservableList<HeuristicSupplier> heuristicSuppliers = FXCollections.observableArrayList();

	private final BooleanProperty canSelectWhitePiecesProperty = new SimpleBooleanProperty(true);
	public boolean getCanSelectWhitePieces() {return canSelectWhitePiecesProperty.get();}
	public void setCanSelectWhitePieces(boolean value) {canSelectWhitePiecesProperty.set(value);}
	public BooleanProperty canSelectWhitePiecesProperty() {return canSelectWhitePiecesProperty;}

	private final BooleanProperty canSelectBlackPiecesProperty = new SimpleBooleanProperty(true);
	public boolean getCanSelectBlackPieces() {return canSelectBlackPiecesProperty.get();}
	public void setCanSelectBlackPieces(boolean value) {canSelectBlackPiecesProperty.set(value);}
	public BooleanProperty canSelectBlackPiecesProperty() {return canSelectBlackPiecesProperty;}

	private final ObjectProperty<Match.State> matchStateProperty = new SimpleObjectProperty<Match.State>();
	public Match.State getMatchState() {return matchStateProperty.get();}
	public void setMatchState(Match.State state) {matchStateProperty.set(state);}
	public ObjectProperty<Match.State> matchStateProperty() {return matchStateProperty;}

	private BooleanProperty playBackControlsDisabledProperty = new SimpleBooleanProperty(false);
	public boolean isPlaybackControlsDisabled() {return playBackControlsDisabledProperty.get();}
	public void setPlaybackControlsDisabled(boolean bool) { playBackControlsDisabledProperty.set(bool);}
	public BooleanProperty getPlayBackControlsDisabledProperty() {return playBackControlsDisabledProperty;}

	private ObjectProperty<AgentSupplier> whiteAgentHolderProperty;
	public AgentSupplier getWhiteAgentHolder() {return whiteAgentHolderProperty.get();}
	public void setWhiteAgentHolder(AgentSupplier agentSupplier) {whiteAgentHolderProperty.set(agentSupplier);}
	public ObjectProperty<AgentSupplier> whiteAgentHolderProperty() {return whiteAgentHolderProperty;}

	private ObjectProperty<AgentSupplier> blackAgentHolderProperty;
	public AgentSupplier getBlackAgentHolder() {return blackAgentHolderProperty.get();}
	public void setBlackAgentHolder(AgentSupplier agentSupplier) {blackAgentHolderProperty.set(agentSupplier);}
	public ObjectProperty<AgentSupplier> blackAgentHolderProperty() {return blackAgentHolderProperty;}

	private ObjectProperty<HeuristicSupplier> whiteHeuristicHolderProperty;
	public HeuristicSupplier getWhiteHeuristicHolder() {return whiteHeuristicHolderProperty.get();}
	public void setWhiteHeuristicHolder(HeuristicSupplier heuristicSupplier) {whiteHeuristicHolderProperty.set(heuristicSupplier);}
	public ObjectProperty<HeuristicSupplier> whiteHeuristicHolderProperty() {return whiteHeuristicHolderProperty;}

	private ObjectProperty<HeuristicSupplier> blackHeuristicHolderProperty;
	public HeuristicSupplier getBlackHeuristicHolder() {return blackHeuristicHolderProperty.get();}
	public void setBlackHeuristicHolder(HeuristicSupplier heuristicSupplier) {blackHeuristicHolderProperty.set(heuristicSupplier);}
	public ObjectProperty<HeuristicSupplier> blackHeuristicHolderProperty() {return blackHeuristicHolderProperty;}

	public Visualizer(Supplier<Match> matchSupplier) {
		this.matchSupplier = matchSupplier;
		this.match = matchSupplier.get();

		menu = new EditContextMenu();
		menu.setAutoHide(true);
		menu.setClearAction(this::clearBoard);
		menu.setKillAction(this::killBoardPiece);
		menu.setKillAllPieces(this::killAllPieces);
		menu.setKillAllPlayer(this::killAllPieces);
		menu.setResetAction(this::resetBoard);
		menu.setSpawnAction(this::spawnBoardPiece);

		playBackView = new PlayBackView();
		playBackView.prefWidthProperty().bind(widthProperty());
		playBackView.setPrefHeight(30);

		moveTimer = new AdjustableTimer();
		playBackView.animationRateProperty().addListener(((observable, oldValue, newValue) -> moveTimer.adjust(newValue.intValue())));

		boardView = new ChessBoard();
		boardView.layoutYProperty().bind(playBackView.heightProperty());
		boardView.layoutXProperty().bind(widthProperty().divide(2).subtract(boardView.widthProperty().divide(2)));
		boardView.prefHeightProperty().bind(heightProperty().subtract(playBackView.heightProperty()));
		boardView.prefWidthProperty().bind(widthProperty());
		boardView.setSquareMousePressedHandler(this::onSquarePressed);
		boardView.showThreatsProperty().bind(playBackView.showThreatsProperty());
		boardView.canSelectBlackProperty().bind(canSelectBlackPiecesProperty());
		boardView.canSelectWhiteProperty().bind(canSelectWhitePiecesProperty());

		setOnMouseMoved(this::onMouseMove);
		setOnMouseExited(this::onMouseExited);

		getChildren().add(playBackView);
		getChildren().add(boardView);

		boardView.paint();

		newMatch();
	}

	public void setPieceMoveHandler(ChessBoard.PieceMoveHandler handler) {
		boardView.setPieceMoveHandler(handler);
	}

	public ChessBoard.PieceMoveHandler getPieceMoveHandler() {
		return boardView.getPieceMoveHandler();
	}

	/**
	 * Actions
	 */

	private void spawnBoardPiece(Piece piece, int x, int y) {
		if (match.getState() != Match.State.PREPARED) return;

		long kernel = Board.kernelOf(x, y);

		for (Piece p : Piece.values()) {
			long state = match.getBoard().getState(p);

			if ((kernel & state) != 0) {
				match.getBoard().setState(p, state ^ kernel);
			}
		}

		long state = match.getBoard().getState(piece);
		match.getBoard().setState(piece, state | kernel);

		match.getBoard().setInitialState(Arrays.copyOf(match.getBoard().getState(), 12));
		boardView.setBoardState(match.getBoard().getInitialState());
	}

	private void resetBoard() {
		if (match.getState() != Match.State.PREPARED) return;

		match = matchSupplier.get();
		newMatch();
	}

	private void killAllPieces(Piece piece) {
		if (match.getState() != Match.State.PREPARED) return;

		match.getBoard().setState(piece, 0);
		match.getBoard().setInitialState(Arrays.copyOf(match.getBoard().getState(), 12));
		boardView.setBoardState(match.getBoard().getInitialState());
	}

	private void killAllPieces(Player player) {
		if (match.getState() != Match.State.PREPARED) return;

		for (Piece piece : Piece.of(player)) {
			match.getBoard().setState(piece, 0);
		}
		match.getBoard().setInitialState(Arrays.copyOf(match.getBoard().getState(), 12));
		boardView.setBoardState(match.getBoard().getInitialState());
	}

	private void killBoardPiece(Piece piece, int x, int y) {
		if (match.getState() != Match.State.PREPARED) return;

		long kernel = Board.kernelOf(x, y);
		long state = match.getBoard().getState(piece);

		if ((kernel & state) != 0) {
			match.getBoard().setState(piece, state ^ kernel);

			match.getBoard().setInitialState(Arrays.copyOf(match.getBoard().getState(), 12));
			boardView.setBoardState(match.getBoard().getInitialState());
		}
	}

	private void clearBoard() {
		if (match.getState() != Match.State.PREPARED) return;

		for (Piece piece : Piece.values()) {
			match.getBoard().setState(piece, 0);
		}

		match.getBoard().setInitialState(Arrays.copyOf(match.getBoard().getState(), 12));
		boardView.setBoardState(match.getBoard().getInitialState());
	}

	private void reset() {
		synchronized (this) {
			pause();
			moveIndex = 0;
			boardView.setBoardState(match.getBoard().getInitialState());
		}
	}

	private void startMatch() {
		final var whiteAgentConfiguration = new AgentConfiguration(playBackView.getDepthLimit(), getWhiteHeuristicHolder().get());
		final var blackAgentConfiguration = new AgentConfiguration(playBackView.getDepthLimit(),  getBlackHeuristicHolder().get());

		match.setWhiteAgent(getWhiteAgentHolder().getSupplier().apply(whiteAgentConfiguration));
		match.setBlackAgent(getBlackAgentHolder().getSupplier().apply(blackAgentConfiguration));

		moveIndex = -1;

		match.start();
	}

	private void interruptMatch() {
		match.interrupt();
	}

	private void newMatch() {
		long[] oldInitialState = match.getBoard().getInitialState();

		match = matchSupplier.get();
		match.getBoard().setInitialState(oldInitialState);
		boardView.setBoardState(match.getBoard().getInitialState());

		match.setOnStateChangeHandler(matchStateProperty::set);
		matchStateProperty.set(Match.State.PREPARED);
	}

	/**
	 * Playback controls
	 */

	public void play() {
		if (!moveTimer.isRunning()) {
			moveTimer.start(() -> {
				nextMove();

				if (moveIndex == match.getMoves().size() - 1 && match.getState() != Match.State.ONGOING) {
					pause();
				}
			}, playBackView.animationRateProperty.getValue());
		}
		playBackView.setIsPlaying(true);
	}

	public void pause() {
		if (moveTimer.isRunning()) {
			moveTimer.shutdown();
		}
		playBackView.setIsPlaying(false);
	}


	public void previousMove() {
		synchronized (this) {
			if (moveIndex > 0) {
				long[] state = match.getMoves().get(--moveIndex);
				boardView.setBoardState(state);
			} else if (moveIndex == 0) {
				long[] state = match.getBoard().getInitialState();
				boardView.setBoardState(state);
				moveIndex--;
			}
		}
	}

	public void nextMove() {
		synchronized (this) {
			if (moveIndex < match.getMoves().size() - 1) {
				long[] state = match.getMoves().get(++moveIndex);
				boardView.setBoardState(state);
			}
		}
	}

	/**
	 * Events
	 */

	private void onMouseExited(MouseEvent event) {
		if (menu.isShowing()) {
			if (event.getX() < 0 || event.getX() >= getWidth() || event.getY() < 0 || event.getY() >= getHeight()) {
				menu.hide();
			}
		}
	}

	private void onMouseMove(MouseEvent event) {
		if (menu.isShowing()) {
			double minX = menu.getAnchorX() - CONTEXT_MENU_HIDE_PADDING;
			double maxX = menu.getAnchorX() + menu.getWidth() + CONTEXT_MENU_HIDE_PADDING;

			double minY = menu.getAnchorY() - CONTEXT_MENU_HIDE_PADDING;
			double maxY = menu.getAnchorY() + menu.getHeight() + CONTEXT_MENU_HIDE_PADDING;
			if (event.getScreenX() < minX || event.getScreenX() > maxX) {
				if (event.getScreenX() < minY || event.getScreenX() > maxY) {
					menu.hide();
				}
			}
		}
	}

	private void onSquarePressed(Piece piece, int x, int y, MouseEvent e) {
		if (e.isSecondaryButtonDown() && match.getState() == Match.State.PREPARED) {
			menu.show(this, e.getScreenX(), e.getScreenY(), piece, x, y);
		}
	}

	private void onPreviousButtonPressed() {
		previousMove();
	}

	private void onNextButtonPressed() {
		nextMove();
	}

	private void onResetButtonPressed() {
		reset();
	}

	private void onPlayPauseButtonPressed() {
		if (moveTimer.isRunning()) {
			pause();
		} else {
			play();
		}
	}

	private void onStartStopButtonPressed() {
		switch (match.getState()) {
			case PREPARED:
				startMatch();
				break;
			case ONGOING:
				interruptMatch();
				break;
			case INTERRUPTED:
			case FINISHED:
				newMatch();
				break;
		}
	}

	private class PlayBackView extends BorderPane {

		private static final int CHILDREN_HEIGHT = 17;

		private final ReadOnlyBooleanProperty showThreatsProperty;
		public boolean getShowThreats() {return showThreatsProperty.get();}
		public ReadOnlyBooleanProperty showThreatsProperty() {return showThreatsProperty;}

		private final IntegerProperty animationRateProperty = new SimpleIntegerProperty(750);
		public int getAnimationRate() {return animationRateProperty.get();}
		public ReadOnlyIntegerProperty animationRateProperty() {return animationRateProperty;}

		private final BooleanProperty isPlayingProperty = new SimpleBooleanProperty(false);
		public boolean getIsPlaying() {return isPlayingProperty.get();}
		public void setIsPlaying(boolean value) {isPlayingProperty.set(value);}
		public BooleanProperty isPlayingProperty() {return isPlayingProperty;}

		private final IntegerProperty depthLimitProperty = new SimpleIntegerProperty(4);
		public int getDepthLimit() {return depthLimitProperty.get();}
		public ReadOnlyIntegerProperty depthLimitProperty() {return depthLimitProperty;}

		public PlayBackView() {
			getStyleClass().add("playback-controls");
			setPadding(new Insets(5, 10, 5, 10));

			final var previousButton = new ImageButton(IO.image(IO.Images.PREVIOUS));
			previousButton.getStyleClass().add("playback-button");
			previousButton.setPrefHeight(CHILDREN_HEIGHT);
			previousButton.setOnAction((ignored) -> onPreviousButtonPressed());
			previousButton.disableProperty().bind(playBackControlsDisabledProperty);
			previousButton.setFocusTraversable(false);

			final var nextButton = new ImageButton(IO.image(IO.Images.NEXT));
			nextButton.getStyleClass().add("playback-button");
			nextButton.setPrefHeight(CHILDREN_HEIGHT);
			nextButton.setOnAction((ignored) -> onNextButtonPressed());
			nextButton.disableProperty().bind(playBackControlsDisabledProperty);
			nextButton.setFocusTraversable(false);

			final var playPauseButton = new ImageButton(IO.image(IO.Images.PLAY));
			playPauseButton.getStyleClass().add("playback-button");
			playPauseButton.setPrefHeight(CHILDREN_HEIGHT);
			playPauseButton.setOnAction((ignored) -> onPlayPauseButtonPressed());
			playPauseButton.disableProperty().bind(playBackControlsDisabledProperty);
			playPauseButton.setFocusTraversable(false);
			isPlayingProperty().addListener((observable, old, now) -> playPauseButton.setImage(this.mapIsPlayingImage(now)));

			final var startStopButton = new ImageButton(IO.image(IO.Images.EXECUTE));
			startStopButton.getStyleClass().add("playback-button");
			startStopButton.setPrefHeight(CHILDREN_HEIGHT);
			startStopButton.setOnAction((ignored) -> onStartStopButtonPressed());
			matchStateProperty().addListener((observable, old, now) -> startStopButton.setImage(this.mapIsExecutingImage(now)));
			startStopButton.setFocusTraversable(false);

			final var resetButton = new ImageButton(IO.image(IO.Images.RESET));
			resetButton.getStyleClass().add("playback-button");
			resetButton.setPrefHeight(CHILDREN_HEIGHT);
			resetButton.setOnAction((ignored) -> onResetButtonPressed());
			resetButton.disableProperty().bind(playBackControlsDisabledProperty);
			resetButton.setFocusTraversable(false);

			final var animationRateSlider = new Slider();
			animationRateSlider.setFocusTraversable(false);
			animationRateSlider.setMin(50);
			animationRateSlider.setMax(1000);
			animationRateSlider.setValue(800);
			animationRateSlider.setShowTickLabels(false);
			animationRateSlider.setShowTickMarks(false);
			animationRateSlider.setMajorTickUnit(10);
			animationRateSlider.setMinorTickCount(5);
			animationRateSlider.setBlockIncrement(10);
			animationRateSlider.setPrefWidth(60);
			animationRateSlider.valueProperty().addListener(((observable, old, now) -> animationRateProperty.set(now.intValue())));

			final var depthLimitTextField = new TextField("4");
			depthLimitTextField.setMaxWidth(100);
			depthLimitTextField.setFocusTraversable(false);
			depthLimitTextField.disableProperty().bind(matchStateProperty().isEqualTo(Match.State.ONGOING));
			depthLimitTextField.setTextFormatter(new TextFormatter<>(change -> {
				String text = change.getText();

				if (text.matches("[0-9]*")) {
					return change;
				}

				return null;
			}));
			depthLimitTextField.textProperty().addListener(((observable, old, now) -> {
				if (now.length() > 0) depthLimitProperty.set(Integer.parseInt(now));
			}));

			final var whiteAgentComboBox = new ComboBox<>(getAgentSuppliers());
			whiteAgentComboBox.disableProperty().bind(matchStateProperty().isEqualTo(Match.State.ONGOING));
			whiteAgentComboBox.setFocusTraversable(false);
			whiteAgentHolderProperty = whiteAgentComboBox.valueProperty();

			final var blackAgentComboBox = new ComboBox<>(getAgentSuppliers());
			blackAgentComboBox.disableProperty().bind(matchStateProperty().isEqualTo(Match.State.ONGOING));
			blackAgentComboBox.setFocusTraversable(false);
			blackAgentHolderProperty = blackAgentComboBox.valueProperty();

			final var blackAgentHeuristicComboBox = new ComboBox<>(getHeuristicSuppliers());
			blackAgentHeuristicComboBox.disableProperty().bind(matchStateProperty().isEqualTo(Match.State.ONGOING));
			blackAgentHeuristicComboBox.setFocusTraversable(false);
			blackHeuristicHolderProperty = blackAgentHeuristicComboBox.valueProperty();

			final var whiteAgentHeuristicComboBox = new ComboBox<>(getHeuristicSuppliers());
			whiteAgentHeuristicComboBox.disableProperty().bind(matchStateProperty().isEqualTo(Match.State.ONGOING));
			whiteAgentHeuristicComboBox.setFocusTraversable(false);
			whiteHeuristicHolderProperty = whiteAgentHeuristicComboBox.valueProperty();

			getAgentSuppliers().addListener((ListChangeListener<AgentSupplier>) c -> {
				final var optional = c.getList().stream().findFirst();
				optional.ifPresent(whiteAgentComboBox.valueProperty()::setValue);
				optional.ifPresent(blackAgentComboBox.valueProperty()::setValue);
			});

			getHeuristicSuppliers().addListener((ListChangeListener<HeuristicSupplier>) c -> {
				final var optional = c.getList().stream().findFirst();
				optional.ifPresent(whiteAgentHeuristicComboBox.valueProperty()::setValue);
				optional.ifPresent(blackAgentHeuristicComboBox.valueProperty()::setValue);
			});

			final var showThreatsCheckbox = new CheckBox("Show threats");
			showThreatsCheckbox.selectedProperty().setValue(true);
			showThreatsCheckbox.setFocusTraversable(false);
			showThreatsProperty = showThreatsCheckbox.selectedProperty();

			final var depthLimitLabel = new Label("Depth limit:");
			depthLimitLabel.setLabelFor(depthLimitTextField);

			final var whiteLabel = new Label("White:");
			whiteLabel.setLabelFor(whiteAgentComboBox);
			whiteLabel.setMinWidth(40);

			final var blackLabel = new Label("Black:");
			blackLabel.setLabelFor(blackAgentComboBox);

			final var gridPane = new GridPane();
			gridPane.setHgap(10);
			gridPane.setVgap(10);
			gridPane.setPadding(new Insets(10, 10, 10, 10));
			gridPane.add(depthLimitLabel, 0, 0);
			gridPane.add(depthLimitTextField, 1, 0);
			gridPane.add(showThreatsCheckbox, 2, 0);

			gridPane.add(whiteLabel, 0, 1);
			gridPane.add(whiteAgentComboBox, 1, 1);
			gridPane.add(whiteAgentHeuristicComboBox, 2, 1);

			gridPane.add(blackLabel, 0, 2);
			gridPane.add(blackAgentComboBox, 1, 2);
			gridPane.add(blackAgentHeuristicComboBox, 2, 2);

			final var agentsButton = new DropdownPopupButton("Agents", gridPane);
			agentsButton.setPrefHeight(CHILDREN_HEIGHT);
			agentsButton.setContentDisplay(ContentDisplay.RIGHT);
			agentsButton.setFocusTraversable(false);

			final var leftHBox = new HBox(10);
			leftHBox.setAlignment(Pos.CENTER_LEFT);
			leftHBox.getChildren().add(agentsButton);
			leftHBox.getChildren().add(animationRateSlider);

			final var middleHBox = new HBox(10);
			middleHBox.setAlignment(Pos.CENTER);
			middleHBox.getChildren().add(previousButton);
			middleHBox.getChildren().add(playPauseButton);
			middleHBox.getChildren().add(nextButton);

			final var rightHBox = new HBox(10);
			rightHBox.setAlignment(Pos.CENTER_RIGHT);
			rightHBox.getChildren().add(resetButton);
			rightHBox.getChildren().add(startStopButton);
			rightHBox.prefWidthProperty().bind(leftHBox.widthProperty());

			setLeft(leftHBox);
			setCenter(middleHBox);
			setRight(rightHBox);
		}

		private Image mapIsPlayingImage(boolean isPlaying) {
			return (isPlaying) ? IO.image(IO.Images.PAUSE) : IO.image(IO.Images.PLAY);
		}

		private Image mapIsExecutingImage(Match.State state) {
			switch (state) {
				case PREPARED:
					return IO.image(IO.Images.EXECUTE);
				case ONGOING:
					return IO.image(IO.Images.STOP);
				default:
					return IO.image(IO.Images.TRASH);
			}
		}

	}

}
