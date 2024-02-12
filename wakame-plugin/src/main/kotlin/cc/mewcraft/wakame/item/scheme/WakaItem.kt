package cc.mewcraft.wakame.item.scheme

import cc.mewcraft.wakame.crate.BinaryCrate
import cc.mewcraft.wakame.item.binary.WakaItemStack
import cc.mewcraft.wakame.item.scheme.cell.SchemeCell
import cc.mewcraft.wakame.item.scheme.meta.MaterialMeta
import cc.mewcraft.wakame.item.scheme.meta.SchemeMeta
import cc.mewcraft.wakame.item.scheme.meta.SchemeMetaKeys
import net.kyori.adventure.key.Key
import net.kyori.adventure.key.Keyed
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.UUID

/**
 * Represents an **item template**, or a "blueprint" in other words.
 * Essentially, this is a representation of the item in the configuration
 * file.
 *
 * The design philosophy of `this` is, that you can use a [WakaItem] as
 * a **blueprint** to create as many [WakaItemStacks][WakaItemStack] as
 * you want by calling [WakaItem.createItemStack], where each of the
 * [ItemStack] will have the data of different values, and even have the
 * data of different types. This allows us to create more possibilities
 * for items, achieving better game experience by randomizing the item
 * generation and hence reducing duplication.
 *
 * @see WakaItemStack
 */
interface WakaItem : Keyed {
    /**
     * The UUID of this item.
     */
    val uuid: UUID

    /**
     * The [key][Key] of this item, where:
     * - [namespace][Key.namespace] is the name of the directory which contains
     *   the config file
     * - [value][Key.value] is the name of the config file itself without the
     *   file extension
     */
    val key: Key

    /**
     * The map holds all the metadata of `this` item. Use your IDE to navigate
     * the subclasses of [SchemeMeta] for all types of metadata.
     *
     * It should be noted that only necessary metadata should be written to
     * the item's NBT while generating an [ItemStack] from `this` [WakaItem].
     * The data that can be derived from other metadata such as [MaterialMeta]
     * should not be written to the NBT.
     *
     * @see getSchemeMetaByClass
     */
    val schemeMeta: Map<Key, SchemeMeta<*>> // TODO why not use the ClassToInstanceMap?

    /**
     * The map holds all the cell data about `this` item, where `map key` is
     * cell ID and `map value` is [SchemeCell].
     *
     * The underlying map is actually an ordered map, and the iteration order
     * is always the same as the insertion order.
     */
    val schemeCells: Map<String, SchemeCell>

    /**
     * Generates an [ItemStack] from this scheme.
     *
     * @param player the player from whom the item is generated
     * @return an once-off [WakaItemStack]
     */
    fun createItemStack(player: Player?): WakaItemStack

    /**
     * Generates an [ItemStack] from this scheme.
     *
     * @param crate the crate from which the item is generated
     * @return an once-off [WakaItemStack]
     */
    fun createItemStack(crate: BinaryCrate): WakaItemStack

    // region Java interface overrides
    override fun key(): Key = key
    // endregion
}

////// Extension functions //////

/**
 * Gets specified [SchemeMeta] from this [WakaItem].
 *
 * This function allows you to quickly get specified [SchemeMeta] by
 * corresponding class reference. You can use this function as the
 * following:
 * ```kotlin
 * val item: WakaItem = < ...... >
 * val meta: ElementMeta = wakaItem.getSchemeMeta<ElementMeta>()
 * ```
 *
 * @param V the subclass of [SchemeMeta]
 * @return the instance of class [V] from this [WakaItem]
 */
inline fun <reified V : SchemeMeta<*>> WakaItem.getSchemeMetaByClass(): V {
    val key = SchemeMetaKeys.get<V>()
    val meta = checkNotNull(schemeMeta[key])
    return meta as V
}