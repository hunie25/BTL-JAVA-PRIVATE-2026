package com.myapp.util;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class UiUtils {

    /**
     * Chạy tác vụ nặng (như gọi API) dưới background thread,
     * sau đó cập nhật kết quả lên giao diện (JavaFX Application Thread).
     * * @param task      Tác vụ cần chạy (VD: movieService::getNewMovies)
     * @param onSuccess Hàm xử lý kết quả khi thành công (VD: movies -> { ... })
     * @param <T>       Kiểu dữ liệu trả về
     */
    public static <T> void runAsync(Callable<T> task, Consumer<T> onSuccess) {
        CompletableFuture.supplyAsync(() -> {
            try {
                return task.call();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }).thenAccept(result -> {
            if (result != null) {
                Platform.runLater(() -> onSuccess.accept(result));
            }
        });
    }

    /**
     * Hiển thị hộp thoại báo lỗi đơn giản.
     */
    public static void showError(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    /**
     * Hiển thị hộp thoại thông báo (Info).
     */
    public static void showInfo(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}