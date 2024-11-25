package cc.mewcraft.wakame.api.block;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

public interface NekoBlockState {

    /**
     * Gets the {@link NekoBlock} of this {@link NekoBlockState}.
     *
     * @return The block of this block state.
     */
    @NotNull NekoBlock getBlock();

    /**
     * Gets the {@link Location} of this {@link NekoBlockState}.
     *
     * @return The location of this block state.
     */
    @NotNull Location getLocation();

}