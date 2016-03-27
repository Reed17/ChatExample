package com.monochord.chat.client;


import com.monochord.chat.client.ui.GuiUtils;
import com.monochord.chat.client.ui.MainForm;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;

public final class ChatClient extends Application {

    public static void main(String[] args) {
        launch(args);
    }


    private static Stage primaryStage;


    public static Stage getPrimaryStage() {
        if (!Platform.isFxApplicationThread())
            throw new IllegalStateException("This method can be called only from Java FX Application Thread");
        return primaryStage;
    }


    @Override
    public void start(Stage primaryStage) throws Exception {
        ChatClient.primaryStage = primaryStage;
        primaryStage.setTitle("Chat Client");

        Thread.currentThread().setUncaughtExceptionHandler(
                (thread, throwable) -> GuiUtils.exceptionAlert(throwable, primaryStage));

        try {
            MainForm mainForm = MainForm.create();
            primaryStage.setOnCloseRequest(event -> mainForm.shutdown());
            Scene scene = new Scene(mainForm);
            scene.getStylesheets().add(getClass().getResource("ui/style.css").toExternalForm());
            primaryStage.setScene(scene);
            primaryStage.show();
        }
        catch (Throwable t) {
            t.printStackTrace();
            System.exit(999);
        }
    }

}
