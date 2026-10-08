package org.marketplace.fx;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

public class FxForm {

    private final Dialog<ButtonType> dialog = new Dialog<>();
    private final VBox fields = new VBox(10);
    private final Label error = new Label();

    private final ButtonType save = new ButtonType(
            "Confirm",
            ButtonBar.ButtonData.OK_DONE
    );

    public FxForm(Window owner, String title) {
        dialog.initOwner(owner);
        dialog.setTitle(title);

        dialog.getDialogPane()
                .getButtonTypes()
                .addAll(save, ButtonType.CANCEL);

        fields.setPadding(new Insets(20));
        fields.setPrefWidth(420);

        error.setWrapText(true);
        error.setStyle("-fx-text-fill: #b91c1c;");

        dialog.getDialogPane().setContent(
                new VBox(12, fields, error)
        );
    }

    public void add(String label, Node control) {
        fields.getChildren().addAll(
                new Label(label),
                control
        );
    }

    public TextField text(String label, String value) {
        TextField field = new TextField(value);
        add(label, field);
        return field;
    }

    public boolean show(final Runnable validation) {
        dialog.getDialogPane()
                .lookupButton(save)
                .addEventFilter(
                        ActionEvent.ACTION,
                        new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent event) {
                                try {
                                    if (validation != null) {
                                        validation.run();
                                    }
                                } catch (RuntimeException exception) {
                                    error.setText(
                                            exception.getMessage()
                                    );
                                    event.consume();
                                }
                            }
                        }
                );

        dialog.showAndWait();

        return save.equals(dialog.getResult());
    }
}