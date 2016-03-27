package com.monochord.chat.client.ui;


import javafx.concurrent.Task;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

class BackgroundExecutor {

    private final ExecutorService executor = Executors.newCachedThreadPool(task -> {
        Thread t = new Thread(task);
        t.setDaemon(true);
        return t;
    });


    @FunctionalInterface
    interface VoidCallable {

        void call() throws Exception;

        default Callable<Void> toCallable() {
            return () -> {call(); return null;};
        }
    }


    void exec(VoidCallable action, Consumer<Void> onSuccess, Consumer<Throwable> onFailure) {
        exec(action.toCallable(), onSuccess, onFailure);
    }


    <V> void exec(Callable<? extends V> action, Consumer<? super V> onSuccess, Consumer<Throwable> onFailure) {
        Task<V> task = newTaskFor(action);
        if (onSuccess != null)
            task.setOnSucceeded(event -> onSuccess.accept(task.getValue()));
        if (onFailure != null)
            task.setOnFailed(event -> onFailure.accept(task.getException()));
        executor.submit(task);
    }


    private static <V> Task<V> newTaskFor(Callable<? extends V> action) {
        return new Task<V>() {
            @Override
            protected V call() throws Exception {
                return action.call();
            }
        };
    }


    void shutdown() {
        executor.shutdown();
    }

}
