package cc.mewcraft.wakame.api.block;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

public interface KoishBlockState {

    /**
     * Gets the {@link KoishBlock} of this {@link KoishBlockState}.
     *
     * @return the block of this block state
     */
    @NotNull KoishBlock getBlock();

    /**
     * Gets the {@link Location} of this {@link KoishBlockState}.
     *
     * @return the location of this block state
     */
    @NotNull Location getLocation();

}