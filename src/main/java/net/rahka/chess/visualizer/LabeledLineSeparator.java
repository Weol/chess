package net.rahka.chess.visualizer;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;

public class LabeledLineSeparator extends Pane {

    private Line firstLine = new Line(), secondLine = new Line();
    private Label label = new Label();

    private final DoubleProperty labelOffsetRatioProperty = new SimpleDoubleProperty(0.2);
    public double getLabelOffsetRatio() {return labelOffsetRatioProperty.get();}
    public void setLabelOffsetRatio(double value) {labelOffsetRatioProperty.set(value);}
    public DoubleProperty labelOffsetRatioProperty() {return labelOffsetRatioProperty;}

    private final DoubleProperty labelPaddingProperty = new SimpleDoubleProperty(5);
    public double getLabelPadding() {return labelPaddingProperty.get();}
    public void setLabelPadding(double value) {labelPaddingProperty.set(value);}
    public DoubleProperty labelPaddingProperty() {return labelPaddingProperty;}

    private final ObjectProperty<Color> colorProperty = new SimpleObjectProperty<Color>(Color.valueOf("#808080"));
    public Color getColor() {return colorProperty.get();}
    public void setColor(Color color) {colorProperty.set(color);}
    public ObjectProperty<Color> colorProperty() {return colorProperty;}

    private final ObjectProperty<String> textProperty = new SimpleObjectProperty<String>("");
    public String getText() {return textProperty.get();}
    public void setText(String string) {textProperty.set(string);}
    public ObjectProperty<String> textProperty() {return textProperty;}

    public LabeledLineSeparator(String text) {
        this(text, 0.2);
    }

    public LabeledLineSeparator(String text, double ratio) {
        setText(text);

        label.textProperty().bind(textProperty());
        label.textFillProperty().bind(colorProperty());

        labelOffsetRatioProperty().set(ratio);

        firstLine.strokeProperty().bind(colorProperty());
        secondLine.strokeProperty().bind(colorProperty());

        firstLine.startXProperty().set(0);
        firstLine.startYProperty().bind(heightProperty().divide(2));
        firstLine.endXProperty().bind(widthProperty().multiply(labelOffsetRatioProperty()).subtract(labelPaddingProperty()));
        firstLine.endYProperty().bind(firstLine.startYProperty());

        label.layoutXProperty().bind(widthProperty().multiply(labelOffsetRatioProperty()));
        label.layoutYProperty().set(0);

        secondLine.startXProperty().bind(label.layoutXProperty().add(label.widthProperty()).add(labelPaddingProperty()));
        secondLine.startYProperty().bind(firstLine.startYProperty());
        secondLine.endXProperty().bind(widthProperty());
        secondLine.endYProperty().bind(firstLine.startYProperty());

        getChildren().add(firstLine);
        getChildren().add(secondLine);
        getChildren().add(label);
    }

}
