package cc.mewcraft.wakame.item.scheme

import cc.mewcraft.wakame.adventure.Keyed
import cc.mewcraft.wakame.item.binary.NekoItemStack
import cc.mewcraft.wakame.item.scheme.cell.SchemeCell
import cc.mewcraft.wakame.item.scheme.meta.SchemeItemMeta
import cc.mewcraft.wakame.item.scheme.meta.SchemeItemMetaKeys
import net.kyori.adventure.key.Key
import java.util.UUID

/**
 * Represents an **item template**, or a "blueprint" in other words.
 * Essentially, this is a representation of the item in the configuration
 * file.
 *
 * The design philosophy of `this` is, that you can use a [NekoItem] as
 * a **blueprint** to create as many [NekoItemStacks][NekoItemStack] as
 * you want by calling [NekoItemRealizer.realize], where each of the
 * ItemStack will have the data of different values, and even have the
 * data of different types. This allows us to create more possibilities
 * for items, achieving better game experience by randomizing the item
 * generation and hence reducing duplication.
 *
 * @see NekoItemStack
 */
interface NekoItem : Keyed {
    /**
     * The UUID of this item.
     */
    val uuid: UUID

    /**
     * The [key][Key] of this item, where:
     * - [namespace][Key.namespace] is the name of the directory which contains the config file
     * - [value][Key.value] is the name of the config file itself (without the file extension)
     */
    override val key: Key

    /**
     * The [key][Key] to the Material of this item.
     */
    val material: Key

    /**
     * The map holds all the item meta of `this` item. You may navigate
     * the subclasses of [SchemeItemMeta] for all types of item meta.
     *
     * It should be noted that only necessary item meta should be written to
     * the item's NBT while generating an ItemStack from `this` [NekoItem].
     *
     * @see getItemMetaBy
     */
    val itemMeta: Map<Key, SchemeItemMeta<*>> // TODO why not use the ClassToInstanceMap?

    /**
     * The map holds all the cell data about `this` item, where `map key` is
     * cell ID and `map value` is [SchemeCell].
     *
     * The underlying map is actually an ordered map, and the iteration order
     * is always the same as the insertion order.
     */
    val cells: Map<String, SchemeCell>
}

/**
 * Gets specified [SchemeItemMeta] from this [NekoItem].
 *
 * This function allows you to quickly get specified [SchemeItemMeta] by
 * corresponding class reference. You can use this function as the
 * following:
 * ```kotlin
 * val item: NekoItem = < ...... >
 * val meta: ElementMeta = item.getItemMetaBy<ElementMeta>()
 * ```
 *
 * @param V the subclass of [SchemeItemMeta]
 * @return the instance of class [V] from this [NekoItem]
 */
inline fun <reified V : SchemeItemMeta<*>> NekoItem.getItemMetaBy(): V {
    val key = SchemeItemMetaKeys.get<V>()
    val meta = checkNotNull(itemMeta[key])
    return meta as V
}