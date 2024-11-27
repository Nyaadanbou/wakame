package cc.mewcraft.wakame.api.block;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

public interface NekoBlockState {

    /**
     * Gets the {@link NekoBlock} of this {@link NekoBlockState}.
     *
     * @return the block of this block state
     */
    @NotNull NekoBlock getBlock();

    /**
     * Gets the {@link Location} of this {@link NekoBlockState}.
     *
     * @return the location of this block state
     */
    @NotNull Location getLocation();

}