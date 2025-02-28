package cc.mewcraft.wakame.api.block;

import cc.mewcraft.wakame.api.item.KoishItem;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@SuppressWarnings("unused")
@NullMarked
public interface KoishBlock {

    /**
     * Gets the id of this block type.
     *
     * @return the id of this block type
     */
    Key getId();

    /**
     * Gets the item for this block type, or null if there is none.
     *
     * @return the item for this block type, or null if there is none
     */
    @Nullable
    KoishItem getItem();

    /**
     * Gets the name of this block type.
     *
     * @return the name of this block type
     */
    Component getName();

    /**
     * Gets the plaintext name of this block type.
     *
     * @return the name of this {@link KoishBlock} in plaintext
     */
    String getPlainName();

}