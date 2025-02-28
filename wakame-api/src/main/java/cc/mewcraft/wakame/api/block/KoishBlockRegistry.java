package cc.mewcraft.wakame.api.block;

import net.kyori.adventure.key.Key;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@SuppressWarnings("unused")
@NullMarked
public interface KoishBlockRegistry {

    /**
     * Gets the {@link KoishBlock} with the specified id.
     *
     * @param id the id of the block
     * @return the block with the specified id
     * @throws IllegalArgumentException if there is no block with the specified id
     */
    KoishBlock get(String id);

    /**
     * Gets the {@link KoishBlock} with the specified id.
     *
     * @param id the id of the block
     * @return the block with the specified id
     * @throws IllegalArgumentException if there is no block with the specified id
     */
    KoishBlock get(Key id);

    /**
     * Gets the {@link KoishBlock} with the specified id, or null if there is none.
     *
     * @param id the id of the block
     * @return the block with the specified id, or null if there is none
     */
    @Nullable
    KoishBlock getOrNull(String id);

    /**
     * Gets the {@link KoishBlock} with the specified id, or null if there is none.
     *
     * @param id the id of the block
     * @return the block with the specified id, or null if there is none
     */
    @Nullable
    KoishBlock getOrNull(Key id);

}