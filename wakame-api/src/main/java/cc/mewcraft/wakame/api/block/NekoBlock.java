package cc.mewcraft.wakame.api.block;

import cc.mewcraft.wakame.api.item.NekoItem;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public interface NekoBlock {

    /**
     * Gets the id of this block type.
     *
     * @return The id of this block type.
     */
    @NotNull Key getId();

    /**
     * Gets the item for this block type, or null if there is none.
     *
     * @return The item for this block type, or null if there is none.
     */
    @Nullable NekoItem getItem();

    /**
     * Gets the name of this block type.
     *
     * @return The name of this block type.
     */
    @NotNull Component getName();

    /**
     * Gets the plaintext name of this block type.
     *
     * @param locale The locale to get the name in. Should be in the same format as the language file
     *               names in resource packs (e.g. en_us).
     * @return The name of this {@link NekoBlock} in plaintext.
     */
    @NotNull String getPlaintextName(@NotNull String locale);

}
