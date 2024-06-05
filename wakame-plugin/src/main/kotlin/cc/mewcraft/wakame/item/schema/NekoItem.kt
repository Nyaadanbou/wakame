package cc.mewcraft.wakame.item.schema

import cc.mewcraft.wakame.adventure.Keyed
import cc.mewcraft.wakame.config.ConfigProvider
import cc.mewcraft.wakame.crate.Crate
import cc.mewcraft.wakame.item.ItemBehaviorAccessor
import cc.mewcraft.wakame.item.ItemSlot
import cc.mewcraft.wakame.item.binary.NekoStack
import cc.mewcraft.wakame.item.binary.PlayNekoStack
import cc.mewcraft.wakame.item.schema.cell.SchemaCell
import cc.mewcraft.wakame.item.schema.meta.SchemaItemMeta
import cc.mewcraft.wakame.user.User
import net.kyori.adventure.key.Key
import org.koin.mp.KoinPlatform
import java.util.UUID
import kotlin.reflect.KClass

/**
 * Represents an **item template**, or a "blueprint" in other words.
 * Essentially, this is a representation of the item in the configuration
 * file.
 *
 * The design philosophy of `this` is, that you can use a [NekoItem] as
 * a **blueprint** to create as many [NekoStacks][NekoStack] as
 * you want by calling [NekoItemRealizer.realize], where each of the
 * ItemStack will have the data of different values, and even have the
 * data of different types. This allows us to create more possibilities
 * for items, achieving better game experience by randomizing the item
 * generation and hence reducing duplication.
 *
 * @see NekoStack
 */
interface NekoItem : Keyed, ItemBehaviorAccessor {
    /**
     * The [key][Key] of this item, where:
     * - [namespace][Key.namespace] is the name of the directory which contains the config file
     * - [value][Key.value] is the name of the config file itself (without the file extension)
     */
    override val key: Key

    /**
     * The UUID of this item.
     */
    val uuid: UUID

    /**
     * The [config provider][ConfigProvider] of this item.
     */
    val config: ConfigProvider

    /**
     * The [key][Key] to the Material of this item.
     */
    val material: Key

    /**
     * The inventory slot where this item can take effect.
     */
    val slot: ItemSlot

    /**
     * The set holds all the schema item meta of this item.
     */
    val meta: Set<SchemaItemMeta<*>>

    /**
     * Gets specific schema item meta by class.
     *
     * It should be noted that this function will always return a non-null
     * schema item meta, no matter whether the schema item meta will generate
     * a data or not. To check the generation result without passing a context,
     * use [SchemaItemMeta.isEmpty].
     *
     * @param T the type of schema item meta
     * @param metaClass the class of schema item meta
     * @return the specific schema item meta
     */
    fun <T : SchemaItemMeta<*>> getMeta(metaClass: KClass<T>): T

    /**
     * The set holds all the schema cells of this item.
     */
    val cell: Set<SchemaCell>

    /**
     * The map holds a mapping from the identifier to schema cell.
     */
    val cellMap: Map<String, SchemaCell>
}

inline fun <reified M : SchemaItemMeta<*>> NekoItem.getMeta(): M {
    return getMeta(M::class)
}

fun NekoItem.reify(context: SchemaGenerationContext): PlayNekoStack {
    return realizer.realize(this, context)
}

fun NekoItem.reify(user: User<*>): PlayNekoStack {
    return realizer.realize(this, user)
}

fun NekoItem.reify(crate: Crate): PlayNekoStack {
    return realizer.realize(this, crate)
}

private val realizer: NekoItemRealizer by KoinPlatform.getKoin().inject()
