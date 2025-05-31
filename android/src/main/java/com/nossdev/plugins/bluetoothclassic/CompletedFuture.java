package com.nossdev.plugins.bluetoothclassic;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Consumer;

public final class CompletedFuture<T> {

    private final CompletableFuture<T> future;

    private Consumer<T> successHandler = t -> {};

    private Consumer<Throwable> errorHandler = e -> {};

    private CompletedFuture(CompletableFuture<T> future) {
        this.future = future;
    }

    public static <T> CompletedFuture<T> from(CompletableFuture<T> future) {
        return new CompletedFuture<>(future);
    }

    public CompletedFuture<T> onSuccess(Consumer<T> successHandler) {
        this.successHandler = successHandler;
        return this;
    }

    public CompletedFuture<T> onError(Consumer<Throwable> errorHandler) {
        this.errorHandler = errorHandler;
        return this;
    }

    public void await() {
        try {
            T result = future.join(); // blocking
            successHandler.accept(result);
        } catch (CompletionException e) {
            errorHandler.accept(e.getCause());
        }
    }
}
