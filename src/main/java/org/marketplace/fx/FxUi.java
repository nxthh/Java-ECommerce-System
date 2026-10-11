package org.marketplace.fx;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Window;
import javafx.util.Callback;

import org.marketplace.model.TableData;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class FxUi {

    private FxUi() {
    }

    public static Button button(
            String text,
            final Runnable action
    ) {
        Button button = new Button(text);

        button.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                action.run();
            }
        });

        return button;
    }

    public static TableView<String[]> table() {
        TableView<String[]> table = new TableView<>();

        table.getStyleClass().add("marketplace-table");
        table.setPlaceholder(new Label("No records found."));

        table.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN
        );

        return table;
    }

    public static void fill(
            TableView<String[]> table,
            TableData data
    ) {
        table.getItems().clear();
        table.getColumns().clear();

        if (data == null || data.getHeader() == null) {
            return;
        }

        String[] headers = data.getHeader();

        List<TableColumn<String[], String>> columns =
                new ArrayList<>();

        for (int i = 0; i < headers.length; i++) {
            final int index = i;
            final String key = headers[i];

            TableColumn<String[], String> column =
                    new TableColumn<>(
                            key.replace('_', ' ')
                                    .toUpperCase(Locale.ROOT)
                    );

            column.setSortable(false);

            configureColumnWidth(column, key);

            // Center all values beneath their headers.
            column.setStyle("-fx-alignment: CENTER;");

            column.setCellValueFactory(
                    new Callback<
                            TableColumn.CellDataFeatures<String[], String>,
                            ObservableValue<String>
                            >() {
                        @Override
                        public ObservableValue<String> call(
                                TableColumn.CellDataFeatures<String[], String> cell
                        ) {
                            String[] row = cell.getValue();
                            String value = "";

                            if (row != null
                                    && index < row.length
                                    && row[index] != null) {
                                value = row[index];
                            }

                            if ("is_active".equalsIgnoreCase(key)) {
                                if ("t".equalsIgnoreCase(value)
                                        || "true".equalsIgnoreCase(value)) {
                                    value = "Active";
                                } else if ("f".equalsIgnoreCase(value)
                                        || "false".equalsIgnoreCase(value)) {
                                    value = "Inactive";
                                }
                            }

                            return new ReadOnlyStringWrapper(value);
                        }
                    }
            );

            columns.add(column);
        }

        // Add all columns together.
        table.getColumns().setAll(columns);

        if (data.getRow() != null) {
            table.getItems().addAll(data.getRow());
        }
    }

    private static void configureColumnWidth(
            TableColumn<String[], String> column,
            String key
    ) {
        String name = key.toLowerCase(Locale.ROOT);

        switch (name) {
            case "id":
                column.setMinWidth(60);
                column.setPrefWidth(70);
                column.setMaxWidth(90);
                break;

            case "product_id":
            case "order_id":
            case "customer_id":
            case "seller_id":
            case "category_id":
            case "user_id":
                column.setMinWidth(100);
                column.setPrefWidth(110);
                column.setMaxWidth(140);
                break;

            case "name":
            case "full_name":
                column.setMinWidth(200);
                column.setPrefWidth(300);
                break;

            case "category":
            case "seller":
            case "username":
                column.setMinWidth(130);
                column.setPrefWidth(170);
                break;

            case "price":
            case "unit_price":
            case "subtotal":
            case "total_amount":
            case "seller_total":
            case "amount":
            case "revenue":
                column.setMinWidth(110);
                column.setPrefWidth(140);
                break;

            case "stock":
            case "quantity":
            case "rating":
            case "units":
            case "orders":
                column.setMinWidth(85);
                column.setPrefWidth(100);
                column.setMaxWidth(130);
                break;

            case "is_active":
                column.setText("STATUS");
                column.setMinWidth(100);
                column.setPrefWidth(110);
                column.setMaxWidth(140);
                break;

            case "status":
            case "role":
            case "method":
                column.setMinWidth(120);
                column.setPrefWidth(150);
                break;

            case "comment":
                column.setMinWidth(240);
                column.setPrefWidth(380);
                break;

            case "created_at":
                column.setMinWidth(180);
                column.setPrefWidth(220);
                break;

            case "sale_date":
                column.setMinWidth(130);
                column.setPrefWidth(160);
                break;

            default:
                column.setMinWidth(110);
                column.setPrefWidth(160);
                break;
        }
    }

    public static void message(
            Window owner,
            String title,
            String text
    ) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);

        if (owner != null) {
            alert.initOwner(owner);
        }

        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(text);
        alert.showAndWait();
    }

    public static boolean confirm(
            Window owner,
            String text
    ) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);

        if (owner != null) {
            alert.initOwner(owner);
        }

        alert.setTitle("Confirm action");
        alert.setHeaderText(null);
        alert.setContentText(text);
        alert.showAndWait();

        return ButtonType.OK.equals(alert.getResult());
    }

    public static String errorText(Throwable error) {
        if (error == null) {
            return "Operation failed.";
        }

        for (
                Throwable cause = error;
                cause != null;
                cause = cause.getCause()
        ) {
            if (cause instanceof SQLException) {
                SQLException sqlError = (SQLException) cause;
                String state = sqlError.getSQLState();

                if ("23505".equals(state)) {
                    return "This record already exists.";
                }

                if ("42P01".equals(state)) {
                    return "A required database table is missing.";
                }

                if ("42703".equals(state)) {
                    return "A required database column is missing.";
                }

                if ("23503".equals(state)) {
                    return "A related record is missing, "
                            + "or this record is still being used.";
                }

                if ("23514".equals(state)) {
                    return "A value does not meet the database requirements.";
                }

                if ("23502".equals(state)) {
                    return "A required value is missing.";
                }

                if ("28P01".equals(state)) {
                    return "Check your database username and password.";
                }

                if (state != null && state.startsWith("08")) {
                    return "Cannot connect to PostgreSQL. "
                            + "Check Docker and db.properties.";
                }

                if (state != null) {
                    return "Database operation failed. SQL state: " + state;
                }

                return "Database operation failed.";
            }
        }

        String message = error.getMessage();

        if (message == null || message.isBlank()) {
            return "Operation failed.";
        }

        return message;
    }

    /*
     * call() runs on a background thread.
     * onSuccess() runs on the JavaFX application thread.
     */
    public abstract static class Job<T> extends Task<T> {

        protected abstract void onSuccess(T result);

        public final void submit(
                final Node scope,
                final Label status
        ) {
            scope.setDisable(true);
            status.setText("Working...");

            setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent event) {
                    scope.setDisable(false);
                    status.setText("");
                    onSuccess(getValue());
                }
            });

            setOnFailed(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent event) {
                    scope.setDisable(false);
                    status.setText(errorText(getException()));
                }
            });

            setOnCancelled(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent event) {
                    scope.setDisable(false);
                    status.setText("Operation cancelled.");
                }
            });

            Thread thread = new Thread(
                    this,
                    "marketplace-background"
            );

            thread.setDaemon(true);
            thread.start();
        }
    }
}