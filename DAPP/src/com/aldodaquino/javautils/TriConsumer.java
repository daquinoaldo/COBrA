package com.aldodaquino.javautils;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Like Consumer or BiConsumer but with three parameters.
 * @see BiConsumer
 * @see Consumer
 */
@FunctionalInterface
public interface TriConsumer<T, U, V> {

    void accept(T t, U u, V v);

    default TriConsumer<T, U, V> andThen(TriConsumer<? super T, ? super U, ? super V> after) {
        Objects.requireNonNull(after);

        return (t, u, v) -> {
            accept(t, u, v);
            after.accept(t, u, v);
        };
    }
}

