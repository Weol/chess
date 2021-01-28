package net.rahka.chess;


import javafx.scene.image.Image;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;

public class IO {

	@RequiredArgsConstructor
	@AllArgsConstructor
	public enum Images {
		WHITE_BISHOP("w_bishop.png"),
		WHITE_KING("w_king.png"),
		WHITE_KNIGHT("w_knight.png"),
		WHITE_PAWN("w_pawn.png"),
		WHITE_QUEEN("w_queen.png"),
		WHITE_ROOK("w_rook.png"),
		BLACK_BISHOP("b_bishop.png"),
		BLACK_KING("b_king.png"),
		BLACK_KNIGHT("b_knight.png"),
		BLACK_PAWN("b_pawn.png"),
		BLACK_QUEEN("b_queen.png"),
		BLACK_ROOK("b_rook.png"),
		PLAY("play.png", "#22bb63"),
		PAUSE("pause.png", "#22bb63"),
		NEXT("next.png", "#22bb63"),
		PREVIOUS("previous.png", "#22bb63"),
		STOP("stop.png", "#22bb63"),
		EXECUTE("play.png", "#22bb63"),
		RESET("loop.png", "#22bb63"),
		CARET_DOWN("caret_down.png", "#22bb63"),
		TRASH("trash.png", "#22bb63"),
		SETTINGS("settings.png", "#22bb63");

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
