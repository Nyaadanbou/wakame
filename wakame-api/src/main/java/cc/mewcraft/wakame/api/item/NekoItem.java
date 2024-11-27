package cc.mewcraft.wakame.api.item;

import cc.mewcraft.wakame.api.block.NekoBlock;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface NekoItem {

    /**
     * Gets the {@link Key} of this item.
     *
     * @return The {@link Key} of this item.
     */
    @NotNull Key getId();

    /**
     * Gets the {@link NekoBlock} this item is associated with, or null if there is none.
     *
     * @return The {@link NekoBlock} this item is associated with, or null if there is none.
     */
    @Nullable NekoBlock getBlock();

    /**
     * Gets the name of this {@link NekoItem}.
     *
     * @return The name of this {@link NekoItem}.
     */
    @NotNull Component getName();

    /**
     * Gets the plaintext name of this {@link NekoItem}.
     *
     * @return The name of this {@link NekoItem} in plaintext.
     */
    @NotNull String getPlainName();

    /**
     * Creates an {@link ItemStack} of this {@link NekoItem} with the specified amount.
     *
     * @param amount The amount of items in the stack.
     * @return An {@link ItemStack} of this {@link NekoItem} with the specified amount.
     */
    @NotNull ItemStack createItemStack(int amount);

    /**
     * Creates an {@link ItemStack} of this {@link NekoItem} with the specified amount.
     *
     * @param amount The amount of items in the stack.
     * @param player The player to create the item stack for.
     * @return An {@link ItemStack} of this {@link NekoItem} with the specified amount.
     */
    @NotNull ItemStack createItemStack(int amount, @NotNull Player player);

    /**
     * Creates an {@link ItemStack} of this {@link NekoItem} with the amount of 1.
     *
     * @return An {@link ItemStack} of this {@link NekoItem} with the amount of 1.
     */
    default @NotNull ItemStack createItemStack() {
        return createItemStack(1);
    }

    /**
     * Creates an {@link ItemStack} of this {@link NekoItem} with the amount of 1.
     *
     * @param player The player to create the item stack for.
     * @return An {@link ItemStack} of this {@link NekoItem} with the amount of 1.
     */
    default @NotNull ItemStack createItemStack(@NotNull Player player) {
        return createItemStack(1, player);
    }

}
