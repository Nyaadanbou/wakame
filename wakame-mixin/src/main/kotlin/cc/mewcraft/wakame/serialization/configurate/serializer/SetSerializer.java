package cc.mewcraft.wakame.serialization.configurate.serializer;

import io.leangen.geantyref.GenericTypeReflector;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.serialize.AbstractListChildSerializer;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.util.CheckedConsumer;

import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Type;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Source: {@code org.spongepowered.configurate.serialize.SetSerializer}
 */
public final class SetSerializer extends AbstractListChildSerializer<Set<?>> {

    @SuppressWarnings("PMD")
    public static boolean accepts(final Type type) {
        final Class<?> erased = GenericTypeReflector.erase(type);
        return Set.class.isAssignableFrom(erased) && (erased.isAssignableFrom(EnumSet.class) || erased.isAssignableFrom(LinkedHashSet.class));
    }

    public SetSerializer() {}

    @Override
    protected AnnotatedType elementType(final AnnotatedType containerType) throws SerializationException {
        if (!(containerType instanceof AnnotatedParameterizedType)) {
            throw new SerializationException("Raw types are not supported for collections");
        }
        return ((AnnotatedParameterizedType) containerType).getAnnotatedActualTypeArguments()[0];
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Set<?> createNew(final int length, final AnnotatedType elementType) {
        final Class<?> erased = GenericTypeReflector.erase(elementType.getType());
        if (erased.isEnum()) {
            return EnumSet.noneOf(erased.asSubclass(Enum.class));
        }
        return new LinkedHashSet<>(length);
    }

    @Override
    protected void forEachElement(final Set<?> collection, final CheckedConsumer<Object, SerializationException> action) throws SerializationException {
        for (final Object el : collection) {
            action.accept(el);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    protected void deserializeSingle(final int index,
                                     final Set<?> collection, final @Nullable Object deserialized) {
        ((Set) collection).add(deserialized);
    }

}
