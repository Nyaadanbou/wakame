package cc.mewcraft.wakame.util.gson.typeadapters.immutable.optional;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

abstract class OptionalTypeAdapter<E, T> extends TypeAdapter<E> {
    private final TypeAdapter<T> elementAdapter;

    OptionalTypeAdapter(TypeAdapter<T> elementAdapter) {
        this.elementAdapter = elementAdapter;
    }

    protected abstract boolean isPresent(E e);

    protected abstract T getValue(E e);

    protected abstract E presentOptional(T t);

    protected abstract E emptyOptional();

    public void write(JsonWriter out, E value) throws IOException {
        if (value == null || !isPresent(value)) {
            out.nullValue();
            return;
        }

        elementAdapter.write(out, getValue(value));
    }

    public E read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return emptyOptional();
        }

        return presentOptional(elementAdapter.read(in));
    }
}
