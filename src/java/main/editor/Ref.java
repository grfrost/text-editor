package editor;

import java.util.function.Consumer;
import java.util.function.Supplier;

class Ref<T> implements Supplier<T>, Consumer<T> {
    private T value;


    Ref(T value) {
        this.value = value;
    }


    static <T> Ref<T> of(T value) {
        return new Ref<>(value);
    }

    @Override
    public T get() {
        return value;
    }

    @Override
    public void accept(T t) {
        value = t;
    }
}
