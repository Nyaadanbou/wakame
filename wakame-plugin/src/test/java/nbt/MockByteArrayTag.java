package nbt;

import cc.mewcraft.nbt.ByteArrayTag;
import cc.mewcraft.nbt.ByteTag;
import cc.mewcraft.nbt.Tag;
import net.kyori.adventure.nbt.BinaryTagTypes;
import net.kyori.adventure.nbt.ByteArrayBinaryTag;

public class MockByteArrayTag extends MockTag implements ByteArrayTag {

    private ByteArrayBinaryTag delegate;

    public MockByteArrayTag(byte[] data) {
        this.delegate = ByteArrayBinaryTag.byteArrayBinaryTag(data);
    }

    @Override public byte[] value() {
        return delegate.value();
    }

    @Override public ByteTag set(final int i, final ByteTag tag) {
        throw new UnsupportedOperationException();
    }

    @Override public void add(final int i, final ByteTag tag) {
        throw new UnsupportedOperationException();
    }

    @Override public ByteTag remove(final int i) {
        throw new UnsupportedOperationException();
    }

    @Override public boolean setTag(final int index, final Tag element) {
        throw new UnsupportedOperationException();
    }

    @Override public boolean addTag(final int index, final Tag element) {
        throw new UnsupportedOperationException();
    }

    @Override public byte elementTypeId() {
        return BinaryTagTypes.BYTE.id();
    }

    @Override public boolean add(final ByteTag e) {
        throw new UnsupportedOperationException();
    }

    @Override public ByteTag get(final int index) {
        return new MockByteTag(delegate.get(index));
    }

    @Override public boolean contains(final ByteTag e) {
        for (final byte b : delegate.value()) {
            if (e.byteValue() == b) {
                return true;
            }
        }
        return false;
    }

    @Override public boolean remove(final ByteTag e) {
        throw new UnsupportedOperationException();
    }

    @Override public int size() {
        return delegate.size();
    }

    @Override public byte getTypeId() {
        return delegate.type().id();
    }

    @Override public String asString() {
        return delegate.toString();
    }
}
