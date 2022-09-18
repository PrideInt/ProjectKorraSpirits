package me.pride.spirits.util.objects;

import java.util.Objects;

@FunctionalInterface
public interface TetraConsumer<T, U, V, W> {
	void accept(T t, U u, V v, W w);
	
	default TetraConsumer<T, U, V, W> andThen(TetraConsumer<T, U, V, W> after) {
		Objects.requireNonNull(after);
		return (t, u, v, w) -> {
			accept(t, u, v, w);
			after.accept(t, u, v, w);
		};
	}
}
