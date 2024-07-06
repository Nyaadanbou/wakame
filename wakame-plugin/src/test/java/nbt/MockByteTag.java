package nbt;

import cc.mewcraft.nbt.ByteTag;
import net.kyori.adventure.nbt.ByteBinaryTag;

public class MockByteTag extends MockTag implements ByteTag {
    private final ByteBinaryTag delegate;

    public MockByteTag(byte value) {
        this.delegate = ByteBinaryTag.byteBinaryTag(value);
    }

    @Override public long longValue() {
        return delegate.longValue();
    }

    @Override public int intValue() {
        return delegate.intValue();
    }

    @Override public short shortValue() {
        return delegate.shortValue();
    }

    @Override public byte byteValue() {
        return delegate.byteValue();
    }

    @Override public double doubleValue() {
        return delegate.doubleValue();
    }

    @Override public float floatValue() {
        return delegate.floatValue();
    }

    @Override public byte getTypeId() {
        return delegate.type().id();
    }

    @Override public String asString() {
        return delegate.toString();
    }

    @Override public ByteTag byteValueOf(final byte value) {
        throw new UnsupportedOperationException();
    }

    @Override public ByteTag byteValueOf(final boolean value) {
        throw new UnsupportedOperationException();
    }
}
