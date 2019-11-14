package net.rahka.visualizer;

import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import net.rahka.IO;

public class ImageButton extends Button {

	ImageView imageView;

	public ImageButton(Image image) {
		super();

		imageView = new ImageView(image);
		imageView.fitHeightProperty().bind(prefHeightProperty());
		imageView.fitWidthProperty().bind(prefWidthProperty());
		imageView.setPreserveRatio(true);

		setGraphic(imageView);
	}

	public void setImage(Image image) {
		imageView.setImage(image);
	}

	public Image getImage(){
		return imageView.getImage();
	}

}
