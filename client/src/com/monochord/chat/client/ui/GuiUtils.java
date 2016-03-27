package com.monochord.chat.client.ui;


import com.monochord.chat.common.data.Credentials;
import javafx.concurrent.Service;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

public class GuiUtils {

    private GuiUtils() {}


    // TODO Generate appropriate message based on server response code for ClientException
    public static void exceptionAlert(Throwable throwable, Window dialogOwner) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        if (dialogOwner != null)
            alert.initOwner(dialogOwner);

        Set<Throwable> chain = new HashSet<>();
        Throwable original = null;
        for (Throwable cause; (cause = throwable.getCause()) != null && chain.add(cause); )
            original = cause;
        if (original != null) {
            Throwable cause = original.getCause();
            if (cause != null)
                original = cause;
        }
        alert.setContentText(original != null ? original.getMessage() : throwable.getMessage());

        StringWriter stackTrace = new StringWriter();
        throwable.printStackTrace(new PrintWriter(stackTrace, true));
        alert.getDialogPane().setExpandableContent(new TextArea(stackTrace.toString()));

        alert.showAndWait();
    }


    static Throwable maskExecutionException(Throwable ex) {
        if (ex instanceof ExecutionException) {
            Throwable cause = ex.getCause();
            if (cause != null)
                return cause;
        }
        return ex;
    }


    static void handleServiceException(Service<?> service) {
        Throwable ex = maskExecutionException(service.getException());
        if (ex != null)
            System.err.println(ex.getMessage());
    }


    static Optional<Credentials> logInDialog(Window owner) {
        int width = 300;

        Label serverLabel = new Label("Server");
        TextField serverField = new TextField();
        serverField.setPrefWidth(width);
        serverField.setText("http://localhost:8080/");

        Label loginLabel = new Label("Login");
        TextField loginField = new TextField();
        loginField.setPrefWidth(width);

        Label passwordLabel = new Label("Password");
        PasswordField passwordField = new PasswordField();
        passwordField.setPrefWidth(width);

        VBox content = new VBox(serverLabel, serverField, loginLabel, loginField, passwordLabel, passwordField);
        content.setPadding(new Insets(5, 5, 5, 5));
        content.setSpacing(5);

        Dialog<Credentials> dialog = new Dialog<>();
        dialog.setTitle("Log-In Credentials");
        if (owner != null)
            dialog.initOwner(owner);

        ButtonType ok = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getButtonTypes().addAll(ok, cancel);
        dialogPane.setContent(content);

        dialog.setResultConverter(buttonType -> {
            ButtonData buttonData = buttonType.getButtonData();
            if (buttonData == ButtonData.OK_DONE) {
                try {
                    return new Credentials(
                            serverField.getText().trim(),
                            loginField.getText().trim(),
                            passwordField.getText().trim());
                }
                catch (IllegalArgumentException e) {
                    Alert alert = new Alert(Alert.AlertType.ERROR, e.getMessage(),
                            new ButtonType("OK", ButtonBar.ButtonData.OK_DONE));
                    if (owner != null)
                        alert.initOwner(owner);
                    alert.showAndWait();
                }
            }
            return null;
        });

        return dialog.showAndWait();
    }


    static <T extends Region> T loadFxmlComponent(URL location, Supplier<T> instantiator) {
        return uncheck(() -> {
            try (InputStream input = location.openStream()) {
                return loadFxmlComponent(input, instantiator);
            }
        });
    }


    static <T extends Region> T loadFxmlComponent(InputStream input, Supplier<T> instantiator) {
        Objects.requireNonNull(input);
        T component = Objects.requireNonNull(instantiator.get());

        FXMLLoader loader = new FXMLLoader();
        loader.setRoot(component);
        loader.setController(component);
        uncheck(() -> loader.load(input));

        return component;
    }


    private static <T> T uncheck(Callable<T> c) {
        try {
            return c.call();
        }
        catch (RuntimeException e) {
            throw e;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
