package net.rahka.chess.visualizer;

import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Paint;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.rahka.chess.IO;
import net.rahka.chess.game.Piece;

import java.io.PrintWriter;
import java.util.Calendar;
import java.util.function.Consumer;

public class PiecePickerPane extends HBox {

	private static final int PIECE_PADDING = 10;

	@NonNull @Getter
	private Piece[] pieces;

	private ImageView[] imageViews;

	private final DoubleProperty pieceSizeProperty = new SimpleDoubleProperty(70);
	public double getPieceSize() {return pieceSizeProperty.get();}
	public void setPieceSize(double value) {pieceSizeProperty.set(value);}
	public DoubleProperty pieceSizeProperty() {return pieceSizeProperty;}

	private final ObjectProperty<Consumer<Piece>> piecePressedHandlerProperty = new SimpleObjectProperty<>();
	public Consumer<Piece> getPiecePressedHandler() {return piecePressedHandlerProperty.get();}
	public void setPiecePressedHandler(Consumer<Piece> consumer) {piecePressedHandlerProperty.set(consumer);}
	public ObjectProperty<Consumer<Piece>> piecePressedHandlerProperty() {return piecePressedHandlerProperty;}

	public PiecePickerPane(@NonNull Piece[] pieces) {
		getStyleClass().add("piece-picker");

		setPieces(pieces);
	}

	public void setPieces(Piece[] pieces) {
		this.pieces = pieces;

		getChildren().clear();

		imageViews = new ImageView[pieces.length];
		for (int i = 0; i < pieces.length; i++) {
			final Piece piece = pieces[i];
			var view = new ImageView(getPieceImage(piece));
			view.fitWidthProperty().bind(pieceSizeProperty);
			view.fitHeightProperty().bind(pieceSizeProperty);
			view.setCursor(Cursor.HAND);
			view.setOnMousePressed((e) -> onPiecePressed(piece));

			getChildren().add(view);

			imageViews[i] = view;
		}
	}

	private void onPiecePressed(Piece piece) {
		if (getPiecePressedHandler() != null) {
			getPiecePressedHandler().accept(piece);
		}
	}

	private Image getPieceImage(Piece piece) {
		switch (piece) {
			case WHITE_PAWN:
				return IO.image(IO.Images.WHITE_PAWN);
			case WHITE_ROOK:
				return IO.image(IO.Images.WHITE_ROOK);
			case WHITE_KNIGHT:
				return IO.image(IO.Images.WHITE_KNIGHT);
			case WHITE_BISHOP:
				return IO.image(IO.Images.WHITE_BISHOP);
			case WHITE_QUEEN:
				return IO.image(IO.Images.WHITE_QUEEN);
			case WHITE_KING:
				return IO.image(IO.Images.WHITE_KING);
			case BLACK_PAWN:
				return IO.image(IO.Images.BLACK_PAWN);
			case BLACK_ROOK:
				return IO.image(IO.Images.BLACK_ROOK);
			case BLACK_KNIGHT:
				return IO.image(IO.Images.BLACK_KNIGHT);
			case BLACK_BISHOP:
				return IO.image(IO.Images.BLACK_BISHOP);
			case BLACK_QUEEN:
				return IO.image(IO.Images.BLACK_QUEEN);
			case BLACK_KING:
				return IO.image(IO.Images.BLACK_KING);
		}
		return IO.image(IO.Images.BLACK_ROOK);
	}

}
