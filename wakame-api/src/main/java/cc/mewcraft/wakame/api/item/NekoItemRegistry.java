package cc.mewcraft.wakame.api.item;

import net.kyori.adventure.key.Key;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface NekoItemRegistry {

    /**
     * Gets the {@link NekoItem} with the specified id.
     *
     * @param id the id of the item
     * @return the {@link NekoItem} with the specified id
     * @throws IllegalArgumentException if there is no {@link NekoItem} with the specified id
     */
    @NotNull NekoItem get(@NotNull String id);

    /**
     * Gets the {@link NekoItem} with the specified id.
     *
     * @param id the id of the item
     * @return the {@link NekoItem} with the specified id
     * @throws IllegalArgumentException if there is no {@link NekoItem} with the specified id
     */
    @NotNull NekoItem get(@NotNull Key id);

    /**
     * Gets the {@link NekoItem} of the specified {@link ItemStack}.
     *
     * @param itemStack the {@link ItemStack} to get the {@link NekoItem} for
     * @return the {@link NekoItem} of the specified {@link ItemStack}
     * @throws IllegalArgumentException if the {@link ItemStack} is not a Neko item
     */
    @NotNull NekoItem get(@NotNull ItemStack itemStack);

    /**
     * Gets the {@link NekoItem} with the specified id, or null if there is none.
     *
     * @param id the id of the item
     * @return the {@link NekoItem} with the specified id, or null if there is none
     */
    @Nullable NekoItem getOrNull(@NotNull String id);

    /**
     * Gets the {@link NekoItem} with the specified id, or null if there is none.
     *
     * @param id the id of the item
     * @return the {@link NekoItem} with the specified id, or null if there is none
     */
    @Nullable NekoItem getOrNull(@NotNull Key id);

    /**
     * Gets the {@link NekoItem} of the specified {@link ItemStack}, or null if it is not a Neko item.
     *
     * @param itemStack the {@link ItemStack} to get the {@link NekoItem} for
     * @return the {@link NekoItem} of the specified {@link ItemStack}, or null if it is not a Neko item
     */
    @Nullable NekoItem getOrNull(@Nullable ItemStack itemStack);

    /**
     * Gets a list of {@link NekoItem NekoItems} with the specified name, ignoring the namespace.
     *
     * @param name the name of the item
     * @return a list of {@link NekoItem NekoItems} with the specified name, ignoring the namespace
     */
    @NotNull List<@NotNull NekoItem> getNonNamespaced(@NotNull String name);

}
