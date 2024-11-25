package cc.mewcraft.wakame.api.block;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@SuppressWarnings("unused")
public interface BlockManager {

    /**
     * Checks if there is a {@link NekoBlockState} at that location.
     *
     * @param location The location to check.
     * @return If there is a {@link NekoBlockState} at that location.
     */
    boolean hasBlock(@NotNull Location location);

    /**
     * Gets the {@link NekoBlockState} at that location.
     *
     * @param location The location of the block.
     * @return The Neko block state or null if there isn't one at that location.
     */
    @Nullable NekoBlockState getBlock(@NotNull Location location);

    /**
     * Places the block at that location.
     *
     * @param location The location where the block should be placed.
     * @param block    The type of the block
     */
    default void placeBlock(@NotNull Location location, @NotNull NekoBlock block) {
        placeBlock(location, block, null);
    }

    /**
     * Places the block at that location.
     *
     * @param location The location where the block should be placed.
     * @param block    The type of the block.
     * @param source   The source of this block placement. Could be a player, tile-entity or similar.
     */
    default void placeBlock(@NotNull Location location, @NotNull NekoBlock block, @Nullable Object source) {
        placeBlock(location, block, source, true);
    }

    /**
     * Places the block at that location.
     *
     * @param location  The location where the block should be placed.
     * @param block     The type of the block.
     * @param source    The source of this block placement. Could be a player, tile-entity or similar.
     * @param playSound If block breaking sounds should be placed.
     */
    void placeBlock(@NotNull Location location, @NotNull NekoBlock block, @Nullable Object source, boolean playSound);

    /**
     * Gets the drops of the Neko block at that location or null if there is no Neko block there.
     *
     * @param location The location of the Neko block.
     * @return The list of drops or null if there is no block from Neko at that location.
     */
    default @Nullable List<@NotNull ItemStack> getDrops(@NotNull Location location) {
        return getDrops(location, null);
    }

    /**
     * Gets the drops of the Neko block at that location as if it was mined with the given tool
     * or null if there is no Neko block there.
     *
     * @param location The location of the Neko block.
     * @param tool     The tool that should be used.
     * @return The list of drops or null if there is no block from Neko at that location.
     */
    default @Nullable List<@NotNull ItemStack> getDrops(@NotNull Location location, @Nullable ItemStack tool) {
        return getDrops(location, null, tool);
    }

    /**
     * Gets the drops of the Neko block at that location as if it was mined by source with the
     * given tool or null if there is no Neko block there.
     *
     * @param location The location of the Neko block.
     * @param source   The source of this action. Could be a player, tile-entity or similar.
     * @param tool     The tool that should be used.
     * @return The list of drops or null if there is no block from Neko at that location.
     */
    @Nullable List<@NotNull ItemStack> getDrops(@NotNull Location location, @Nullable Object source, @Nullable ItemStack tool);

    /**
     * Removes the Neko block at that location.
     *
     * @param location The location of the block to remove.
     * @return If there was a Neko block at that location and the removal was successful.
     */
    default boolean removeBlock(@NotNull Location location) {
        return removeBlock(location, null);
    }

    /**
     * Removes the Neko block at that location as if it was destroyed by source.
     *
     * @param location The location of the block to remove.
     * @param source   The source of the block removal.
     * @return If there was a Neko block at that location and the removal was successful.
     */
    boolean removeBlock(@NotNull Location location, @Nullable Object source);

    /**
     * Removes the Neko block at that location as if it was destroyed by source.
     *
     * @param location     The location of the block to remove.
     * @param source       The source of the block removal.
     * @param breakEffects If block breaking effects should be played.
     * @return If there was a Neko block at that location and the removal was successful.
     */
    boolean removeBlock(@NotNull Location location, @Nullable Object source, boolean breakEffects);

}