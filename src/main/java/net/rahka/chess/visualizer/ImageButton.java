package net.rahka.chess.visualizer;

import javafx.beans.property.ObjectProperty;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class ImageButton extends Button {

	private final ImageView imageView;

	public ImageButton(Image image) {
		super();

		imageView = new ImageView(image);
		imageView.fitHeightProperty().bind(prefHeightProperty());
		imageView.fitWidthProperty().bind(prefWidthProperty());
		imageView.setPreserveRatio(true);

		setGraphic(imageView);

		imageProperty = imageView.imageProperty();
	}

	private final ObjectProperty<Image> imageProperty;
	public Image getImage() {return imageProperty.get();}
	public void setImage(Image image) {imageProperty.set(image);}
	public ObjectProperty<Image> imageProperty() {return imageProperty;}

}
