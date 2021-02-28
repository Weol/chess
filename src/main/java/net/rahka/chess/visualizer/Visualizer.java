package net.rahka.chess.visualizer;

import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Line;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.NonNull;
import net.rahka.chess.IO;
import net.rahka.chess.agent.Agent;
import net.rahka.chess.configuration.ConfigurableClass;
import net.rahka.chess.game.Board;
import net.rahka.chess.game.Match;
import net.rahka.chess.game.Piece;
import net.rahka.chess.game.Player;
import net.rahka.chess.utils.AdjustableTimer;

import java.io.IOError;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public class Visualizer extends Pane {

	private static final int CONTEXT_MENU_HIDE_PADDING = 30;

	private final ChessBoard boardView;
	private final PlayBackView playBackView;
	private final EditContextMenu menu;
	private final PiecePickerPane piecePicker;
	private final Pane shadowPane;

	private final AdjustableTimer moveTimer;

	private int moveIndex;

	@NonNull @Getter
	private Match match;

	@Getter
	private final Supplier<Match> matchSupplier;

	@NonNull @Getter
	private final ObservableList<ConfigurableClass<Agent>> agentConfigurables = FXCollections.observableArrayList();

	private final ObjectProperty<String> leftMessageProperty = new SimpleObjectProperty<String>();
	public String getLeftMessage() {return leftMessageProperty.get();}
	public void setLeftMessage(String string) {leftMessageProperty.set(string);}
	public ObjectProperty<String> leftMessageProperty() {return leftMessageProperty;}

	private final ObjectProperty<String> centerMessageProperty = new SimpleObjectProperty<String>();
	public String getCenterMessage() {return centerMessageProperty.get();}
	public void setCenterMessage(String string) {centerMessageProperty.set(string);}
	public ObjectProperty<String> centerMessageProperty() {return centerMessageProperty;}

	private final ObjectProperty<String> rightMessageProperty = new SimpleObjectProperty<String>();
	public String getRightMessage() {return rightMessageProperty.get();}
	public void setRightMessage(String string) {rightMessageProperty.set(string);}
	public ObjectProperty<String> rightMessageProperty() {return rightMessageProperty;}

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

	private final ObjectProperty<ConfigurableClass<Agent>> whiteAgentHolderProperty = new SimpleObjectProperty<>();
	public ConfigurableClass<Agent> getWhiteAgentHolder() {return whiteAgentHolderProperty.get();}
	public void setWhiteAgentHolder(ConfigurableClass<Agent> configurableAgentClass) {whiteAgentHolderProperty.set(configurableAgentClass);}
	public ObjectProperty<ConfigurableClass<Agent>> whiteAgentHolderProperty() {return whiteAgentHolderProperty;}

	private final ObjectProperty<ConfigurableClass<Agent>> blackAgentHolderProperty = new SimpleObjectProperty<>();
	public ConfigurableClass<Agent> getBlackAgentHolder() {return blackAgentHolderProperty.get();}
	public void setBlackAgentHolder(ConfigurableClass<Agent> configurableAgentClass) {blackAgentHolderProperty.set(configurableAgentClass);}
	public ObjectProperty<ConfigurableClass<Agent>> blackAgentHolderProperty() {return blackAgentHolderProperty;}

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

		BorderPane messagePane = new BorderPane();
		messagePane.getStyleClass().add("message-pane");
		messagePane.prefWidthProperty().bind(widthProperty());
		messagePane.layoutXProperty().set(0);
		messagePane.layoutYProperty().bind(heightProperty().subtract(messagePane.heightProperty()));

		var leftMessageLabel = new Label();
		leftMessageLabel.textProperty().bind(leftMessageProperty());
		messagePane.setLeft(leftMessageLabel);

		var centerMessageLabel = new Label();
		centerMessageLabel.textProperty().bind(centerMessageProperty());
		messagePane.setCenter(centerMessageLabel);

		var rightMessageLabel = new Label();
		rightMessageLabel.textProperty().bind(rightMessageProperty());
		messagePane.setRight(rightMessageLabel);

		matchStateProperty().addListener((obs, old, now) -> onMatchStateChange(now));

		boardView = new ChessBoard();
		boardView.layoutYProperty().bind(playBackView.heightProperty());
		boardView.layoutXProperty().bind(widthProperty().divide(2).subtract(boardView.widthProperty().divide(2)));
		boardView.prefHeightProperty().bind(heightProperty().subtract(playBackView.heightProperty()).subtract(messagePane.heightProperty()));
		boardView.prefWidthProperty().bind(widthProperty());
		boardView.setSquareMousePressedHandler(this::onSquarePressed);
		boardView.showThreatsProperty().bind(playBackView.showThreatsProperty());
		boardView.canSelectBlackProperty().bind(canSelectBlackPiecesProperty());
		boardView.canSelectWhiteProperty().bind(canSelectWhitePiecesProperty());

		piecePicker = new PiecePickerPane(Piece.getBlack());
		piecePicker.prefWidth(400);
		piecePicker.prefWidth(200);
		piecePicker.layoutXProperty().bind(widthProperty().divide(2).subtract(piecePicker.widthProperty().divide(2)));
		piecePicker.layoutYProperty().bind(heightProperty().divide(2).subtract(piecePicker.heightProperty().divide(2)));
		piecePicker.pieceSizeProperty().bind(boardView.heightProperty().divide(8));
		piecePicker.setVisible(false);

		shadowPane = new Pane();
		shadowPane.prefWidthProperty().bind(widthProperty());
		shadowPane.prefHeightProperty().bind(heightProperty());
		shadowPane.layoutXProperty().set(0);
		shadowPane.layoutYProperty().set(0);
		shadowPane.getStyleClass().add("shadow-pane");
		shadowPane.visibleProperty().bind(piecePicker.visibleProperty());

		setOnMouseMoved(this::onMouseMove);
		setOnMouseExited(this::onMouseExited);

		getChildren().add(playBackView);
		getChildren().add(boardView);
		getChildren().add(messagePane);
		getChildren().add(shadowPane);
		getChildren().add(piecePicker);

		boardView.paint();

		newMatch();
	}

	public void setPieceMoveHandler(ChessBoard.PieceMoveHandler handler) {
		boardView.setPieceMoveHandler(handler);
	}

	public ChessBoard.PieceMoveHandler getPieceMoveHandler() {
		return boardView.getPieceMoveHandler();
	}

	public Piece showPiecePicker(final Piece[] pieces) {
		final AtomicReference<Piece> pieceReference = new AtomicReference<>();

		Platform.runLater(() -> {
			piecePicker.setPieces(pieces);
			piecePicker.setPiecePressedHandler(p -> {
				synchronized (pieceReference) {
					pieceReference.set(p);
					pieceReference.notifyAll();
				}
			});

			piecePicker.setVisible(true);
		});

		synchronized (pieceReference) {
			while (pieceReference.get() == null) {
				try {
					pieceReference.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		Platform.runLater(() -> {
			piecePicker.setVisible(false);
		});

		return pieceReference.get();
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
			moveIndex = -1;
			boardView.setBoardState(match.getBoard().getInitialState());
		}
	}

	private void startMatch() {
		try {
			match.setWhiteAgent(getWhiteAgentHolder().build());
			match.setBlackAgent(getBlackAgentHolder().build());

			moveIndex = -1;

			match.start();
		} catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
			e.printStackTrace();
		}
	}

	private void interruptMatch() {
		match.interrupt();
	}

	private void newMatch() {
		long[] oldInitialState = match.getBoard().getInitialState();

		match = matchSupplier.get();
		match.getBoard().setInitialState(oldInitialState);
		boardView.setBoardState(match.getBoard().getInitialState());

		match.setOnCurrentPlayerChangeHandler((move) -> Platform.runLater(() -> onCurrentPlayerChange(move)));
		match.setOnStateChangeHandler((state) -> Platform.runLater(() -> setMatchState(state)));
		match.setOnBoardStateChangeHandler((move) -> this.nextMove());
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

	private void onMatchStateChange(Match.State state) {
		switch (state) {
			case PREPARED:
				setCenterMessage("Ready to start match");
				break;
			case ONGOING:
				break;
			case INTERRUPTED:
				setCenterMessage("Match was interrupted");
				break;
			case FINISHED:
				String winner = (getMatch().getWinner().isWhite()) ? "White" : "Black";
				setCenterMessage(winner + " is the winner");
				break;
		}
	}

	private void onCurrentPlayerChange(Player player) {
		String playerName = (player.isWhite()) ? "White" : "Black";

		setCenterMessage(playerName + " player is moving...");
	}

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

		private ClassConfigurer<Agent> whiteConfigurer, blackConfigurer;

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

			final var playBackDisabledProperty = playBackControlsDisabledProperty.or(matchStateProperty.isEqualTo(Match.State.ONGOING)).or(matchStateProperty.isEqualTo(Match.State.PREPARED));

			final var previousButton = new ImageButton(IO.image(IO.Images.PREVIOUS));
			previousButton.getStyleClass().add("playback-button");
			previousButton.setPrefHeight(CHILDREN_HEIGHT);
			previousButton.setOnAction((ignored) -> onPreviousButtonPressed());
			previousButton.disableProperty().bind(playBackDisabledProperty);
			previousButton.setFocusTraversable(false);

			final var nextButton = new ImageButton(IO.image(IO.Images.NEXT));
			nextButton.getStyleClass().add("playback-button");
			nextButton.setPrefHeight(CHILDREN_HEIGHT);
			nextButton.setOnAction((ignored) -> onNextButtonPressed());
			nextButton.disableProperty().bind(playBackDisabledProperty);
			nextButton.setFocusTraversable(false);

			final var playPauseButton = new ImageButton(IO.image(IO.Images.PLAY));
			playPauseButton.getStyleClass().add("playback-button");
			playPauseButton.setPrefHeight(CHILDREN_HEIGHT);
			playPauseButton.setOnAction((ignored) -> onPlayPauseButtonPressed());
			playPauseButton.disableProperty().bind(playBackDisabledProperty);
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
			resetButton.disableProperty().bind(playBackDisabledProperty);
			resetButton.visibleProperty().bind(playBackDisabledProperty.not());
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

			final var whiteAgentComboBox = new ComboBox<>(getAgentConfigurables());
			whiteAgentComboBox.disableProperty().bind(matchStateProperty().isEqualTo(Match.State.ONGOING));
			whiteAgentComboBox.setFocusTraversable(false);
			whiteAgentHolderProperty().bindBidirectional(whiteAgentComboBox.valueProperty());

			final var whiteAgentSettingsButton = new ImageButton(IO.image(IO.Images.SETTINGS));
			whiteAgentSettingsButton.getStyleClass().add("playback-button");
			whiteAgentSettingsButton.disableProperty().bind(matchStateProperty().isEqualTo(Match.State.ONGOING));
			whiteAgentSettingsButton.prefHeightProperty().bind(whiteAgentComboBox.heightProperty().subtract(4));
			whiteAgentSettingsButton.setFocusTraversable(false);
			whiteAgentSettingsButton.onActionProperty().set((ignored) -> onAgentSettingsButtonPressed(Player.WHITE));

			final var blackAgentComboBox = new ComboBox<>(getAgentConfigurables());
			blackAgentComboBox.disableProperty().bind(matchStateProperty().isEqualTo(Match.State.ONGOING));
			blackAgentComboBox.setFocusTraversable(false);
			blackAgentHolderProperty().bindBidirectional(blackAgentComboBox.valueProperty());

			final var blackAgentSettingsButton = new ImageButton(IO.image(IO.Images.SETTINGS));
			blackAgentSettingsButton.getStyleClass().add("playback-button");
			blackAgentSettingsButton.disableProperty().bind(matchStateProperty().isEqualTo(Match.State.ONGOING));
			blackAgentSettingsButton.prefHeightProperty().bind(blackAgentComboBox.heightProperty().subtract(4));
			blackAgentSettingsButton.setFocusTraversable(false);
			blackAgentSettingsButton.onActionProperty().set((ignored) -> onAgentSettingsButtonPressed(Player.BLACK));

			getAgentConfigurables().addListener((ListChangeListener<ConfigurableClass<Agent>>) c -> {
				final var optional = c.getList().stream().findFirst();
				optional.ifPresent(whiteAgentComboBox.valueProperty()::setValue);
				optional.ifPresent(blackAgentComboBox.valueProperty()::setValue);
			});

			final var showThreatsCheckbox = new CheckBox("Show threats");
			showThreatsCheckbox.selectedProperty().setValue(true);
			showThreatsCheckbox.setFocusTraversable(false);
			showThreatsProperty = showThreatsCheckbox.selectedProperty();

			final var whiteLabel = new Label("White:");
			whiteLabel.setLabelFor(whiteAgentComboBox);
			whiteLabel.setMinWidth(40);

			final var blackLabel = new Label("Black:");
			blackLabel.setLabelFor(blackAgentComboBox);

			final var gridPane = new GridPane();
			gridPane.setHgap(10);
			gridPane.setVgap(10);
			gridPane.setPadding(new Insets(10, 10, 10, 10));
			gridPane.add(showThreatsCheckbox, 1, 0);

			gridPane.add(whiteLabel, 0, 1);
			gridPane.add(whiteAgentComboBox, 1, 1);
			gridPane.add(whiteAgentSettingsButton, 2, 1);

			gridPane.add(blackLabel, 0, 2);
			gridPane.add(blackAgentComboBox, 1, 2);
			gridPane.add(blackAgentSettingsButton, 2, 2);

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

		private void onAgentSettingsButtonPressed(Player player) {
			final Stage dialog = new Stage();
			dialog.setTitle("Configuration");
			dialog.initModality(Modality.APPLICATION_MODAL);
			dialog.initOwner(this.getScene().getWindow());

			final int margins = 10;

			var pane = new Pane();

			final var saveButton = new Button("Save");
			saveButton.getStyleClass().add("bordered-button");
			saveButton.getStyleClass().add("playback-button");
			saveButton.layoutXProperty().bind(pane.widthProperty().subtract(saveButton.widthProperty()).subtract(margins));
			saveButton.layoutYProperty().set(margins);
			saveButton.onActionProperty().set(e -> dialog.close());

			pane.getChildren().add(saveButton);

			final var titleLabel = new Label("Configuration for " + whiteAgentHolderProperty.get().getName());
			titleLabel.layoutXProperty().set(10);
			titleLabel.layoutYProperty().bind(saveButton.layoutYProperty().add(saveButton.heightProperty().divide(2).subtract(titleLabel.heightProperty().divide(2))));
			pane.getChildren().add(titleLabel);

			ClassConfigurer<Agent> configurer;
			if (player.isWhite()) {
				if (whiteConfigurer == null) {
					whiteConfigurer = new ClassConfigurer<>(whiteAgentHolderProperty.get());
				}
				whiteConfigurer.setConfigurableClass(whiteAgentHolderProperty.get());
				configurer = whiteConfigurer;
			} else {
				if (blackConfigurer == null) {
					blackConfigurer = new ClassConfigurer<>(blackAgentHolderProperty.get());
				}
				blackConfigurer.setConfigurableClass(blackAgentHolderProperty.get());
				configurer = blackConfigurer;
			}
			configurer.setStyle("-fx-border-width: 1 0 0 0; -fx-border-color: rgb(128, 128, 128);");
			configurer.layoutXProperty().set(0);
			configurer.setPadding(new Insets(10));

			ScrollPane scrollPane = new ScrollPane();
			scrollPane.layoutXProperty().set(0);
			scrollPane.layoutYProperty().bind(saveButton.layoutYProperty().add(saveButton.heightProperty()).add(margins));
			scrollPane.prefWidthProperty().bind(pane.widthProperty());
			scrollPane.prefHeightProperty().bind(pane.heightProperty().subtract(scrollPane.layoutYProperty()));
			scrollPane.setContent(configurer);
			scrollPane.fitToWidthProperty().set(true);
			pane.getChildren().add(scrollPane);

			configurer.prefWidthProperty().bind(pane.widthProperty().subtract(scrollPane.viewportBoundsProperty().get().getWidth()));

			final Scene dialogScene = new Scene(pane, 300, 300);
			dialog.setScene(dialogScene);
			dialog.show();
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
