package cc.mewcraft.lazyconfig.configurate.serializer;

import io.leangen.geantyref.TypeToken;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.serialize.AbstractListChildSerializer;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.util.CheckedConsumer;

import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.util.ArrayList;
import java.util.List;

/**
 * Source: {@code org.spongepowered.configurate.serialize.ListSerializer}
 */
public final class ListSerializer extends AbstractListChildSerializer<List<?>> {

    public static final TypeToken<List<?>> TYPE = new TypeToken<List<?>>() {};

    public ListSerializer() {}

    @Override
    protected AnnotatedType elementType(final AnnotatedType containerType) throws SerializationException {
        if (!(containerType instanceof AnnotatedParameterizedType)) {
            throw new SerializationException(containerType, "Raw types are not supported for collections");
        }
        return ((AnnotatedParameterizedType) containerType).getAnnotatedActualTypeArguments()[0];
    }

    @Override
    protected List<?> createNew(final int length, final AnnotatedType elementType) {
        return new ArrayList<>(length);
    }

    @Override
    protected void forEachElement(final List<?> collection,
                                  final CheckedConsumer<Object, SerializationException> action) throws SerializationException {
        for (final Object el : collection) {
            action.accept(el);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    protected void deserializeSingle(final int index, final List<?> collection, final @Nullable Object deserialized) {
        ((List) collection).add(deserialized);
    }

}
