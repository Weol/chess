package net.rahka.chess.visualizer;

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import net.rahka.chess.configuration.*;

import java.lang.reflect.InvocationTargetException;

public class ClassConfigurer<T> extends GridPane {

    private int currentIndex = 0;

    private ConfigurableClass<T> configurableClass;

    public ClassConfigurer() {
        setVgap(10);
        setHgap(10);
    }

    public ClassConfigurer(ConfigurableClass<T> configurableClass) {
        this();
        if (configurableClass != null) {
            setConfigurableClass(configurableClass);
        }
    }

    public void setConfigurableClass(ConfigurableClass<T> configurableClass) {
        this.configurableClass = configurableClass;

        getChildren().clear();

        currentIndex = 0;

        for (ConfigurableItem dependency : configurableClass.getDependencies()) {
            if (dependency.isConfigurableInteger()) {
                addIntegerField(dependency.asConfigurableInteger());
            } else if (dependency.isConfigurableFloatingPoint()) {
                addFloatingPointField(dependency.asConfigurableFloatingPoint());
            } else if (dependency.isConfigurableBoolean()) {
                addBooleanField(dependency.asConfigurableBoolean());
            } else if (dependency.isConfigurableString()) {
                addStringField(dependency.asConfigurableString());
            }
        }

        for (ConfigurableItem dependency : configurableClass.getDependencies()) {
            if (dependency.isConfigurableClass()) {
                addClassField(dependency.asConfigurableClass());
            }
        }
    }

    private void addClassField(ConfigurableClassItem item) {
        if (item.getClasses().size() == 1) {
            for (var cls : item.getClasses()) {
                if (cls.getDependencies().length == 0) {
                    item.setSupplier(cls::build);
                    return;
                }
            }
        }

        var classes = FXCollections.<ConfigurableClass<?>>observableArrayList();

        ConfigurableClass<?> def = null;
        for (var cls : item.getClasses()) {
            if (def == null) def = cls;

            classes.add(cls);
            if (cls.getCls().equals(item.getDef().getCls())) {
                def = cls;
            }
        }

        var configurer = new ClassConfigurer<>(def);
        configurer.setPadding(new Insets(0, 0, 0, 10));
        configurer.setVisible(def != null);

        var paddingRightProperty = Bindings.createDoubleBinding(() -> getPadding().getRight(), paddingProperty());
        var paddingLeftProperty = Bindings.createDoubleBinding(() -> getPadding().getLeft(), paddingProperty());

        final var line = new LabeledLineSeparator(def != null ? def.getName() : "");
        line.prefWidthProperty().bind(widthProperty().subtract(paddingRightProperty).subtract(paddingLeftProperty));

        final var comboBox = new ComboBox<>(classes);
        comboBox.valueProperty().addListener((observable, old, now) -> onClassSelected(item, now, configurer, line));
        comboBox.setValue(def);

        final var label = new Label(item.getName());
        label.setLabelFor(comboBox);

        item.setSupplier(configurer::buildClass);

        add(line, 0, currentIndex + 1, 2, 1);

        add(configurer, 0, currentIndex + 2, 2, 1);

        add(label, 0, currentIndex);
        add(comboBox, 1, currentIndex);

        currentIndex += 3;
    }

    private <K> void onClassSelected(ConfigurableClassItem item, ConfigurableClass<?> now, final ClassConfigurer<K> configurer, LabeledLineSeparator line) {
        configurer.setConfigurableClass((ConfigurableClass<K>) now);
        configurer.setVisible(now.getDependencies().length > 0);
        line.setVisible(configurer.isVisible());
        line.setText(now.getName());
    }

    private void addStringField(ConfigurableStringItem item) {
        final var textField = new TextField();
        textField.setText(item.getDef());

        final var label = new Label(item.getName());
        label.setLabelFor(textField);

        item.setSupplier(textField::getText);

        add(label, 0, currentIndex);
        add(textField, 1, currentIndex);

        currentIndex++;
    }

    private void addBooleanField(ConfigurableBooleanItem item) {
        final var checkBox = new CheckBox(item.getName());
        checkBox.setSelected(item.getDef());

        item.setSupplier(checkBox::isSelected);

        add(checkBox, 0, currentIndex);

        currentIndex++;
    }

    private void addFloatingPointField(ConfigurableFloatingPointItem item) {
        final var textField = new TextField();
        textField.setText(item.getDef() + "");
        textField.setTextFormatter(new TextFormatter<>(change -> {
            String text = change.getControlNewText();

            if (text.length() == 0) return change;

            try {
                var value = Double.parseDouble(text);
                return change;
            } catch (NumberFormatException e) {
                return null;
            }
        }));

        item.setSupplier(() -> Double.parseDouble(textField.getText()));

        final var label = new Label(item.getName());
        label.setLabelFor(textField);

        add(label, 0, currentIndex);
        add(textField, 1, currentIndex);

        currentIndex++;
    }

    private void addIntegerField(ConfigurableIntegerItem item) {
        final var textField = new TextField();
        textField.setText(item.getDef() + "");
        textField.setTextFormatter(new TextFormatter<>(change -> {
            String text = change.getControlNewText();

            if (text.length() == 0) return change;

            try {
                Long.parseLong(text);
                return change;
            } catch (NumberFormatException e) {
                return null;
            }
        }));

        item.setSupplier(() -> Integer.parseInt(textField.getText()));

        final var label = new Label(item.getName());
        label.setLabelFor(textField);

        add(label, 0, currentIndex);
        add(textField, 1, currentIndex);

        currentIndex++;
    }

    private Object buildClass() {
        try {
            return configurableClass.build();
        } catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
            e.printStackTrace();
        }
        return null;
    }

}
