package nbt;

import cc.mewcraft.nbt.Tag;
import me.lucko.shadow.Shadow;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.io.DataOutput;
import java.io.IOException;

@NullMarked
public abstract class MockTag implements Tag {
    @Override
    public Tag copy() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void write(final DataOutput dataOutput) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Class<? extends Shadow> getShadowClass() {
        throw new UnsupportedOperationException();
    }

    @Override
    public @Nullable Object getShadowTarget() {
        throw new UnsupportedOperationException();
    }
}
