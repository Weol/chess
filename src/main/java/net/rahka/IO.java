package net.rahka;

import com.sun.javafx.scene.shape.SVGPathHelper;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.SVGPath;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;

public class IO {

	@RequiredArgsConstructor
	@AllArgsConstructor
	public enum Images {
		WHITE_BISHOP("pieces/w_bishop.png"),
		WHITE_KING("pieces/w_king.png"),
		WHITE_KNIGHT("pieces/w_knight.png"),
		WHITE_PAWN("pieces/w_pawn.png"),
		WHITE_QUEEN("pieces/w_queen.png"),
		WHITE_ROOK("pieces/w_rook.png"),
		BLACK_BISHOP("pieces/b_bishop.png"),
		BLACK_KING("pieces/b_king.png"),
		BLACK_KNIGHT("pieces/b_knight.png"),
		BLACK_PAWN("pieces/b_pawn.png"),
		BLACK_QUEEN("pieces/b_queen.png"),
		BLACK_ROOK("pieces/b_rook.png"),
		PLAY("play.png", "#22bb63"),
		PAUSE("pause.png", "#22bb63"),
		NEXT("next.png", "#22bb63"),
		PREVIOUS("previous.png", "#22bb63"),
		STOP("stop.png", "#22bb63"),
		EXECUTE("play.png", "#22bb63"),
		RESET("loop.png", "#22bb63"),
		CARET_DOWN("caret_down.png", "#22bb63");

		private final String fileName;
		private String color;
	}

	private static HashMap<Images, Image> images = new HashMap<>();

	public static Image image(Images image) {
		Image img;
		if (images.containsKey(image)) {
			img = images.get(image);
		} else {
			img = new Image(image.fileName);
			images.put(image, img);
		}
		return img;
	}


}
