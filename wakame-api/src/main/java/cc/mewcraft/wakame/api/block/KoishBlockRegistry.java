package cc.mewcraft.wakame.api.block;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public interface KoishBlockRegistry {

    /**
     * Gets the {@link KoishBlock} with the specified id.
     *
     * @param id the id of the block
     * @return the block with the specified id
     * @throws IllegalArgumentException if there is no block with the specified id
     */
    @NotNull KoishBlock get(@NotNull String id);

    /**
     * Gets the {@link KoishBlock} with the specified id.
     *
     * @param id the id of the block
     * @return the block with the specified id
     * @throws IllegalArgumentException if there is no block with the specified id
     */
    @NotNull KoishBlock get(@NotNull Key id);

    /**
     * Gets the {@link KoishBlock} with the specified id, or null if there is none.
     *
     * @param id the id of the block
     * @return the block with the specified id, or null if there is none
     */
    @Nullable KoishBlock getOrNull(@NotNull String id);

    /**
     * Gets the {@link KoishBlock} with the specified id, or null if there is none.
     *
     * @param id the id of the block
     * @return the block with the specified id, or null if there is none
     */
    @Nullable KoishBlock getOrNull(@NotNull Key id);

}