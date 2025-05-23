package cc.mewcraft.wakame.api.tileentity;

import cc.mewcraft.wakame.api.block.KoishBlock;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;

@SuppressWarnings("unused")
@NullMarked
public interface TileEntity {

    /**
     * Gets the owner of this {@link TileEntity}.
     *
     * @return the owner of this {@link TileEntity}
     */
    @Nullable
    OfflinePlayer getOwner();

    /**
     * Gets the {@link KoishBlock} of this {@link TileEntity}.
     *
     * @return the {@link KoishBlock} of this {@link TileEntity}
     */
    KoishBlock getBlock();

    /**
     * Gets the {@link Location} of this {@link TileEntity}.
     *
     * @return the {@link Location} of this {@link TileEntity}
     */
    Location getLocation();

    /**
     * Retrieves a list of all {@link ItemStack ItemStacks} this {@link TileEntity} would drop.
     *
     * @param includeSelf whether to include the tile entity itself in the drops
     * @return a list of all {@link ItemStack ItemStacks} this {@link TileEntity} would drop
     */
    List<ItemStack> getDrops(boolean includeSelf);

}