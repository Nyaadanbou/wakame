package cc.mewcraft.wakame.api.block;

import cc.mewcraft.wakame.api.tileentity.TileEntity;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public interface NekoTileEntityState extends NekoBlockState {

    /**
     * Gets the {@link TileEntity} represented by this {@link NekoTileEntityState}.
     *
     * @return The {@link TileEntity} represented by this {@link NekoTileEntityState}.
     */
    @NotNull TileEntity getTileEntity();

}