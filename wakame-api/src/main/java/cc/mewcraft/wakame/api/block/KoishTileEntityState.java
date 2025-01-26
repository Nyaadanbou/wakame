package cc.mewcraft.wakame.api.block;

import cc.mewcraft.wakame.api.tileentity.TileEntity;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public interface KoishTileEntityState extends KoishBlockState {

    /**
     * Gets the {@link TileEntity} represented by this {@link KoishTileEntityState}.
     *
     * @return the {@link TileEntity} represented by this {@link KoishTileEntityState}
     */
    @NotNull TileEntity getTileEntity();

}