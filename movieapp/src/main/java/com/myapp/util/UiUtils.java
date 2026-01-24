package com.myapp.util;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Modality;

import java.util.concurrent.Callable;
import java.util.function.Consumer;

public class UiUtils {

    public static <T> void runAsync(Callable<T> backgroundTask, Consumer<T> uiUpdate) {
        Task<T> task = new Task<>() {
            @Override
            protected T call() throws Exception {
                return backgroundTask.call();
            }
        };

        // Khi chạy xong thành công
        task.setOnSucceeded(event -> {
            T result = task.getValue();
            // Platform.runLater đảm bảo code chạy trên luồng giao diện
            Platform.runLater(() -> uiUpdate.accept(result));
        });

        // Khi gặp lỗi
        task.setOnFailed(event -> {
            Throwable e = task.getException();
            e.printStackTrace();
            Platform.runLater(() -> showError("Lỗi tải dữ liệu", e.getMessage()));
        });


        new Thread(task).start();
    }

    /**
     * Hiển thị thông báo lỗi đơn giản
     */
    public static void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.initModality(Modality.APPLICATION_MODAL);

        // CSS cho Alert để nó không bị "lệch tông" so với Dark Mode của app
        // (Yêu cầu bạn phải add file css vào DialogPane nếu muốn đẹp hoàn hảo)
        alert.showAndWait();
    }
}