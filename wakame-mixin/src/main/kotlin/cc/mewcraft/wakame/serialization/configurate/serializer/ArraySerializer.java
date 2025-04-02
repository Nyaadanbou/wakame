package cc.mewcraft.wakame.serialization.configurate.serializer;

import io.leangen.geantyref.GenericTypeReflector;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.serialize.AbstractListChildSerializer;
import org.spongepowered.configurate.serialize.Scalars;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.util.CheckedConsumer;
import org.spongepowered.configurate.util.Types;

import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Array;
import java.lang.reflect.Type;

/**
 * A serializer for array classes. Primitive arrays need special handling
 * because they don't have autoboxing like single primitives do.
 * <p>
 * Source: {@code org.spongepowered.configurate.serialize.ArraySerializer}
 *
 * @param <T> array type
 */
public abstract class ArraySerializer<T> extends AbstractListChildSerializer<T> {

    public ArraySerializer() {}

    @Override
    protected AnnotatedType elementType(final AnnotatedType containerType) throws SerializationException {
        final AnnotatedType componentType = GenericTypeReflector.getArrayComponentType(containerType);
        if (componentType == null) {
            throw new SerializationException(containerType, "Must be array type");
        }
        return componentType;
    }

    public static final class Objects extends ArraySerializer<Object[]> {

        public static boolean accepts(final Type token) {
            if (!Types.isArray(token)) {
                return false;
            }

            final Type componentType = GenericTypeReflector.getArrayComponentType(token);
            // require that the component type is non-primitive, by comparing with its `box`-ed value
            // this works because `box` is a only a no-op on non-primitive types
            return componentType.equals(GenericTypeReflector.box(componentType));
        }

        @Override
        protected Object[] createNew(final int length, final AnnotatedType elementType) {
            return (Object[]) Array.newInstance(GenericTypeReflector.erase(elementType.getType()), length);
        }

        @Override
        protected void forEachElement(final Object[] collection,
                                      final CheckedConsumer<Object, SerializationException> action) throws SerializationException {
            for (final Object o : collection) {
                action.accept(o);
            }
        }

        @Override
        protected void deserializeSingle(final int index, final Object[] collection, final @Nullable Object deserialized) {
            collection[index] = deserialized;
        }

    }

    public static final class Booleans extends ArraySerializer<boolean[]> {

        public static final Class<boolean[]> TYPE = boolean[].class;

        @Override
        protected boolean[] createNew(final int length, final AnnotatedType elementType) {
            return new boolean[length];
        }

        @Override
        protected void forEachElement(final boolean[] collection,
                                      final CheckedConsumer<Object, SerializationException> action) throws SerializationException {
            for (final boolean b : collection) {
                action.accept(b);
            }
        }

        @Override
        protected void deserializeSingle(final int index, final boolean[] collection,
                                         final @Nullable Object deserialized) throws SerializationException {
            collection[index] = deserialized == null ? false : Scalars.BOOLEAN.deserialize(deserialized);
        }

    }

    public static final class Bytes extends ArraySerializer<byte[]> {

        public static final Class<byte[]> TYPE = byte[].class;

        @Override
        protected byte[] createNew(final int length, final AnnotatedType elementType) {
            return new byte[length];
        }

        @Override
        protected void forEachElement(final byte[] collection,
                                      final CheckedConsumer<Object, SerializationException> action) throws SerializationException {
            for (final byte b : collection) {
                action.accept(b);
            }
        }

        @Override
        protected void deserializeSingle(final int index, final byte[] collection,
                                         final @Nullable Object deserialized) throws SerializationException {
            collection[index] = deserialized == null ? 0 : Scalars.INTEGER.deserialize(deserialized).byteValue();
        }

    }

    public static final class Chars extends ArraySerializer<char[]> {

        public static final Class<char[]> TYPE = char[].class;

        @Override
        protected char[] createNew(final int length, final AnnotatedType elementType) {
            return new char[length];
        }

        @Override
        protected void forEachElement(final char[] collection,
                                      final CheckedConsumer<Object, SerializationException> action) throws SerializationException {
            for (final char b : collection) {
                action.accept(b);
            }
        }

        @Override
        protected void deserializeSingle(final int index, final char[] collection,
                                         final @Nullable Object deserialized) throws SerializationException {
            collection[index] = deserialized == null ? 0 : Scalars.CHAR.deserialize(deserialized);
        }

    }

    public static final class Shorts extends ArraySerializer<short[]> {

        public static final Class<short[]> TYPE = short[].class;

        @Override
        protected short[] createNew(final int length, final AnnotatedType elementType) {
            return new short[length];
        }

        @Override
        protected void forEachElement(final short[] collection,
                                      final CheckedConsumer<Object, SerializationException> action) throws SerializationException {
            for (final short b : collection) {
                action.accept(b);
            }
        }

        @Override
        protected void deserializeSingle(final int index, final short[] collection,
                                         final @Nullable Object deserialized) throws SerializationException {
            collection[index] = deserialized == null ? 0 : Scalars.INTEGER.deserialize(deserialized).shortValue();
        }

    }

    public static final class Ints extends ArraySerializer<int[]> {

        public static final Class<int[]> TYPE = int[].class;

        @Override
        protected int[] createNew(final int length, final AnnotatedType elementType) {
            return new int[length];
        }

        @Override
        protected void forEachElement(final int[] collection,
                                      final CheckedConsumer<Object, SerializationException> action) throws SerializationException {
            for (final int b : collection) {
                action.accept(b);
            }
        }

        @Override
        protected void deserializeSingle(final int index, final int[] collection,
                                         final @Nullable Object deserialized) throws SerializationException {
            collection[index] = deserialized == null ? 0 : Scalars.INTEGER.deserialize(deserialized);
        }

    }

    public static final class Longs extends ArraySerializer<long[]> {

        public static final Class<long[]> TYPE = long[].class;

        @Override
        protected long[] createNew(final int length, final AnnotatedType elementType) {
            return new long[length];
        }

        @Override
        protected void forEachElement(final long[] collection,
                                      final CheckedConsumer<Object, SerializationException> action) throws SerializationException {
            for (final long b : collection) {
                action.accept(b);
            }
        }

        @Override
        protected void deserializeSingle(final int index, final long[] collection,
                                         final @Nullable Object deserialized) throws SerializationException {
            collection[index] = deserialized == null ? 0 : Scalars.LONG.deserialize(deserialized);
        }

    }

    public static final class Floats extends ArraySerializer<float[]> {

        public static final Class<float[]> TYPE = float[].class;

        @Override
        protected float[] createNew(final int length, final AnnotatedType elementType) {
            return new float[length];
        }

        @Override
        protected void forEachElement(final float[] collection,
                                      final CheckedConsumer<Object, SerializationException> action) throws SerializationException {
            for (final float b : collection) {
                action.accept(b);
            }
        }

        @Override
        protected void deserializeSingle(final int index, final float[] collection,
                                         final @Nullable Object deserialized) throws SerializationException {
            collection[index] = deserialized == null ? 0 : Scalars.FLOAT.deserialize(deserialized);
        }

    }

    public static final class Doubles extends ArraySerializer<double[]> {

        public static final Class<double[]> TYPE = double[].class;

        @Override
        protected double[] createNew(final int length, final AnnotatedType elementType) {
            return new double[length];
        }

        @Override
        protected void forEachElement(final double[] collection,
                                      final CheckedConsumer<Object, SerializationException> action) throws SerializationException {
            for (final double b : collection) {
                action.accept(b);
            }
        }

        @Override
        protected void deserializeSingle(final int index, final double[] collection,
                                         final @Nullable Object deserialized) throws SerializationException {
            collection[index] = deserialized == null ? 0 : Scalars.DOUBLE.deserialize(deserialized);
        }

    }

}
