package cc.mewcraft.wakame.api.item;

import net.kyori.adventure.key.Key;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;

@NullMarked
public interface KoishItemRegistry {

    /**
     * Gets the {@link KoishItem} with the specified id.
     *
     * @param id the id of the item
     * @return the {@link KoishItem} with the specified id
     * @throws IllegalArgumentException if there is no {@link KoishItem} with the specified id
     */
    KoishItem get(String id);

    /**
     * Gets the {@link KoishItem} with the specified id.
     *
     * @param id the id of the item
     * @return the {@link KoishItem} with the specified id
     * @throws IllegalArgumentException if there is no {@link KoishItem} with the specified id
     */
    KoishItem get(Key id);

    /**
     * Gets the {@link KoishItem} of the specified {@link ItemStack}.
     *
     * @param itemStack the {@link ItemStack} to get the {@link KoishItem} for
     * @return the {@link KoishItem} of the specified {@link ItemStack}
     * @throws IllegalArgumentException if the {@link ItemStack} is not a Koish item
     */
    KoishItem get(ItemStack itemStack);

    /**
     * Gets the {@link KoishItem} with the specified id, or null if there is none.
     *
     * @param id the id of the item
     * @return the {@link KoishItem} with the specified id, or null if there is none
     */
    @Nullable
    KoishItem getOrNull(@Nullable String id);

    /**
     * Gets the {@link KoishItem} with the specified id, or null if there is none.
     *
     * @param id the id of the item
     * @return the {@link KoishItem} with the specified id, or null if there is none
     */
    @Nullable
    KoishItem getOrNull(@Nullable Key id);

    /**
     * Gets the {@link KoishItem} of the specified {@link ItemStack}, or null if it is not a Koish item.
     *
     * @param itemStack the {@link ItemStack} to get the {@link KoishItem} for
     * @return the {@link KoishItem} of the specified {@link ItemStack}, or null if it is not a Koish item
     */
    @Nullable
    KoishItem getOrNull(@Nullable ItemStack itemStack);

    /**
     * Gets a list of {@link KoishItem KoishItems} with the specified name, ignoring the namespace.
     *
     * @param name the name of the item
     * @return a list of {@link KoishItem KoishItems} with the specified name, ignoring the namespace
     */
    List<KoishItem> getNonNamespaced(String name);

}
