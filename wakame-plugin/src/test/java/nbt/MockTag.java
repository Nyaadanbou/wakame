package nbt;

import cc.mewcraft.nbt.Tag;
import me.lucko.shadow.Shadow;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.DataOutput;
import java.io.IOException;

public abstract class MockTag implements Tag {
    @Override public Tag copy() {
        throw new UnsupportedOperationException();
    }

    @Override public void write(final DataOutput dataOutput) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override public @NonNull Class<? extends Shadow> getShadowClass() {
        throw new UnsupportedOperationException();
    }

    @Override public @Nullable Object getShadowTarget() {
        throw new UnsupportedOperationException();
    }
}
