package cc.mewcraft.wakame.api.item;

import cc.mewcraft.wakame.api.block.KoishBlock;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public interface KoishItem {

    /**
     * Gets the {@link Key} of this item.
     *
     * @return the {@link Key} of this item
     */
    Key getId();

    /**
     * Gets the {@link KoishBlock} this item is associated with, or null if there is none.
     *
     * @return the {@link KoishBlock} this item is associated with, or null if there is none
     */
    @Nullable
    KoishBlock getBlock();

    /**
     * Gets the name of this {@link KoishItem}.
     *
     * @return the name of this {@link KoishItem}
     */
    Component getName();

    /**
     * Gets the plaintext name of this {@link KoishItem}.
     *
     * @return the name of this {@link KoishItem} in plaintext
     */
    String getPlainName();

    /**
     * Creates an {@link ItemStack} of this {@link KoishItem} with the specified amount.
     *
     * @param amount the amount of items in the stack
     * @return an {@link ItemStack} of this {@link KoishItem} with the specified amount
     */
    ItemStack createItemStack(int amount);

    /**
     * Creates an {@link ItemStack} of this {@link KoishItem} with the specified amount.
     *
     * @param amount the amount of items in the stack
     * @param player the player to create the item stack for
     * @return an {@link ItemStack} of this {@link KoishItem} with the specified amount
     */
    ItemStack createItemStack(int amount, @Nullable Player player);

    /**
     * Creates an {@link ItemStack} of this {@link KoishItem} with the amount of 1.
     *
     * @return an {@link ItemStack} of this {@link KoishItem} with the amount of 1
     */
    default ItemStack createItemStack() {
        return createItemStack(1);
    }

    /**
     * Creates an {@link ItemStack} of this {@link KoishItem} with the amount of 1.
     *
     * @param player the player to create the item stack for
     * @return an {@link ItemStack} of this {@link KoishItem} with the amount of 1
     */
    default ItemStack createItemStack(@Nullable Player player) {
        return createItemStack(1, player);
    }

}