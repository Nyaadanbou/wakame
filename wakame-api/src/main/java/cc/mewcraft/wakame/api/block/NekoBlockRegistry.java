package cc.mewcraft.wakame.api.block;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public interface NekoBlockRegistry {

    /**
     * Gets the {@link NekoBlock} with the specified id.
     *
     * @param id The id of the block.
     * @return The block with the specified id.
     * @throws IllegalArgumentException If there is no block with the specified id.
     */
    @NotNull NekoBlock get(@NotNull String id);

    /**
     * Gets the {@link NekoBlock} with the specified id.
     *
     * @param id The id of the block.
     * @return The block with the specified id.
     * @throws IllegalArgumentException If there is no block with the specified id.
     */
    @NotNull NekoBlock get(@NotNull Key id);

    /**
     * Gets the {@link NekoBlock} with the specified id, or null if there is none.
     *
     * @param id The id of the block.
     * @return The block with the specified id, or null if there is none.
     */
    @Nullable NekoBlock getOrNull(@NotNull String id);

    /**
     * Gets the {@link NekoBlock} with the specified id, or null if there is none.
     *
     * @param id The id of the block.
     * @return The block with the specified id, or null if there is none.
     */
    @Nullable NekoBlock getOrNull(@NotNull Key id);

}
