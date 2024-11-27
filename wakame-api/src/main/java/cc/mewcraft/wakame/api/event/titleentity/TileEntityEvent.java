package cc.mewcraft.wakame.api.event.titleentity;

import cc.mewcraft.wakame.api.tileentity.TileEntity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

/**
 * A {@link TileEntity} related event.
 */
public abstract class TileEntityEvent extends Event {

    private final @NotNull TileEntity tileEntity;

    public TileEntityEvent(@NotNull TileEntity tileEntity) {
        this.tileEntity = tileEntity;
    }

    public @NotNull TileEntity getTileEntity() {
        return tileEntity;
    }

    @Override
    public @NotNull String toString() {
        return "TileEntityEvent{" +
               "tileEntity=" + tileEntity +
               '}';
    }

}