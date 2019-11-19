package net.rahka.chess.visualizer;

import javafx.event.ActionEvent;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Popup;
import javafx.stage.Window;
import net.rahka.chess.IO;

public class DropdownPopupButton extends Button {

	private static final int Y_OFFSET = 5;

	private Popup popup;

	public DropdownPopupButton(String s, Node popupNode) {
		super(s);
		var imageView = new ImageView(IO.image(IO.Images.CARET_DOWN));
		imageView.setFitWidth(10);
		imageView.setFitHeight(10);

		setGraphic(imageView);

		setContentDisplay(ContentDisplay.RIGHT);
		getStyleClass().add("dropdown-popup-button");

		StackPane container = new StackPane(popupNode);
		container.getStyleClass().add("popup-container");

		popup = new Popup();
		popup.setAutoHide(true);
		popup.setAutoFix(true);
		popup.setHideOnEscape(true);
		popup.getContent().add(container);

		setOnAction(this::onAction);
	}

	private void onAction(ActionEvent actionEvent) {
		Point2D point = localToScene(0, getHeight());
		Window window = getScene().getWindow();
		if (popup.isShowing()) {
			popup.hide();
		} else {
			popup.show(window, point.getX() + window.getX() + getScene().getX(), point.getY() + window.getY() + getScene().getY() + Y_OFFSET);
		}
	}

}
