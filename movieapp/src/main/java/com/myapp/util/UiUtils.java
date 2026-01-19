package com.myapp.util;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class UiUtils {
    private UiUtils() {}

    // ===== Alert =====
    public static void info(String msg) {
        alert(Alert.AlertType.INFORMATION, "Info", msg);
    }

    public static void error(String msg) {
        alert(Alert.AlertType.ERROR, "Error", msg);
    }

    private static void alert(Alert.AlertType type, String title, String msg) {
        Platform.runLater(() -> {
            Alert a = new Alert(type, msg, ButtonType.OK);
            a.setTitle(title);
            a.setHeaderText(null);
            a.showAndWait();
        });
    }

    // ===== Async helper (QUAN TRỌNG) =====
    public static <T> void runAsync(Supplier<T> task, Consumer<T> onSuccess, Consumer<Throwable> onError) {
        CompletableFuture
                .supplyAsync(task)
                .thenAccept(result -> Platform.runLater(() -> onSuccess.accept(result)))
                .exceptionally(ex -> {
                    Platform.runLater(() -> onError.accept(ex));
                    return null;
                });
    }
}
