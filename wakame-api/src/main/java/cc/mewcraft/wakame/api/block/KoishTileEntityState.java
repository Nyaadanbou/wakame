package cc.mewcraft.wakame.api.block;

import cc.mewcraft.wakame.api.tileentity.TileEntity;
import org.jspecify.annotations.NullMarked;

@SuppressWarnings("unused")
@NullMarked
public interface KoishTileEntityState extends KoishBlockState {

    /**
     * Gets the {@link TileEntity} represented by this {@link KoishTileEntityState}.
     *
     * @return the {@link TileEntity} represented by this {@link KoishTileEntityState}
     */
    TileEntity getTileEntity();

}