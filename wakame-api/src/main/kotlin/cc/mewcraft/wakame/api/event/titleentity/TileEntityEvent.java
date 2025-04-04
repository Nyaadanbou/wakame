package cc.mewcraft.wakame.api.event.titleentity;

import cc.mewcraft.wakame.api.tileentity.TileEntity;
import org.bukkit.event.Event;
import org.jspecify.annotations.NullMarked;

/**
 * A {@link TileEntity} related event.
 */
@NullMarked
public abstract class TileEntityEvent extends Event {

    private final TileEntity tileEntity;

    public TileEntityEvent(TileEntity tileEntity) {
        this.tileEntity = tileEntity;
    }

    public TileEntity getTileEntity() {
        return tileEntity;
    }

    @Override
    public String toString() {
        return "TileEntityEvent{" +
                "tileEntity=" + tileEntity +
                '}';
    }

}