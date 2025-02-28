package cc.mewcraft.wakame.api.block;

import org.bukkit.Location;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface KoishBlockState {

    /**
     * Gets the {@link KoishBlock} of this {@link KoishBlockState}.
     *
     * @return the block of this block state
     */
    KoishBlock getBlock();

    /**
     * Gets the {@link Location} of this {@link KoishBlockState}.
     *
     * @return the location of this block state
     */
    Location getLocation();

}