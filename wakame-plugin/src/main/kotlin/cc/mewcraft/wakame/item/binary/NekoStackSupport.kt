package cc.mewcraft.wakame.item.binary

import cc.mewcraft.wakame.item.BaseBinaryKeys
import cc.mewcraft.wakame.item.EffectiveSlot
import cc.mewcraft.wakame.item.binary.cell.ItemCellAccessor
import cc.mewcraft.wakame.item.binary.cell.ItemCellAccessorImpl
import cc.mewcraft.wakame.item.binary.meta.ItemMetaAccessor
import cc.mewcraft.wakame.item.binary.meta.ItemMetaAccessorImpl
import cc.mewcraft.wakame.item.binary.show.CustomDataAccessor
import cc.mewcraft.wakame.item.binary.stats.ItemStatisticsAccessor
import cc.mewcraft.wakame.item.binary.stats.ItemStatisticsAccessorImpl
import cc.mewcraft.wakame.item.schema.NekoItem
import cc.mewcraft.wakame.item.schema.behavior.ItemBehavior
import cc.mewcraft.wakame.registry.NekoItemRegistry
import cc.mewcraft.wakame.util.*
import me.lucko.helper.nbt.ShadowTagType
import me.lucko.helper.shadows.nbt.CompoundShadowTag
import net.kyori.adventure.key.Key
import org.bukkit.Material
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import java.util.UUID
import kotlin.reflect.KClass

/**
 * This type alias is used to verify whether a PlayNekoStack should be considered
 * "effective" for the player. By "effective", we mean, for example, whether the item
 * is in an effective slot and whether the item has certain behavior enabled in the
 * config.
 */
typealias PlayNekoStackPredicate = PlayNekoStack.() -> Boolean

/**
 * The same as [PlayNekoStackPredicate] but for [ShowNekoStack].
 */
typealias ShowNekoStackPredicate = ShowNekoStack.() -> Boolean

/**
 * Common code shared by [PlayNekoStack] and [ShowNekoStack].
 *
 * This could be used if it does not require the specific code of
 * [PlayNekoStack] and [ShowNekoStack].
 */
internal interface BaseNekoStack : NekoStack {
    /**
     * Gets the "wakame" [Compound][CompoundShadowTag] of this item.
     *
     * This **does not** include any other tags which are **not** part of the
     * wakame item NBT specifications, such as display name, lore, enchantment and
     * durability, which are already accessible via the Bukkit API. To get access to
     * those tags, just use the wrapped [itemStack].
     */
    val tags: CompoundShadowTag

    //<editor-fold desc="Getters">
    override val isNmsBacked: Boolean
        get() = itemStack.isNmsObjectBacked

    override val isNeko: Boolean
        get() = itemStack.nekoCompoundOrNull != null

    override val isPlay: Boolean
        get() = !isShow // an NS is either PNS or SNS

    override val isShow: Boolean
        get() = tags.contains(BaseBinaryKeys.SHOW, ShadowTagType.BYTE)

    override val schema: NekoItem
        get() = NekoItemRegistry.INSTANCES[key]

    override val key: Key
        get() = Key(namespace, path)

    override val namespace: String
        get() = tags.getString(BaseBinaryKeys.NAMESPACE)

    override val path: String
        get() = tags.getString(BaseBinaryKeys.PATH)

    override val variant: Int
        get() = tags.getInt(BaseBinaryKeys.VARIANT)

    override val uuid: UUID
        get() = NekoItemRegistry.INSTANCES[key].uuid

    override val effectiveSlot: EffectiveSlot
        get() = NekoItemRegistry.INSTANCES[key].effectiveSlot

    override val cell: ItemCellAccessor
        get() = ItemCellAccessorImpl(this)

    override val meta: ItemMetaAccessor
        get() = ItemMetaAccessorImpl(this)

    override val statistics: ItemStatisticsAccessor
        get() = ItemStatisticsAccessorImpl(this)
    //</editor-fold>

    //<editor-fold desc="Setters">
    override fun erase() {
        itemStack.removeNekoCompound()
    }

    override fun putRoot(compoundTag: CompoundShadowTag) {
        itemStack.nekoCompound = compoundTag
    }

    override fun putKey(key: Key) {
        putNamespace(key.namespace())
        putPath(key.value())
    }

    override fun putNamespace(namespace: String) {
        tags.putString(BaseBinaryKeys.NAMESPACE, namespace)
    }

    override fun putPath(path: String) {
        tags.putString(BaseBinaryKeys.PATH, path)
    }

    override fun putVariant(sid: Int) {
        tags.putInt(BaseBinaryKeys.VARIANT, sid)
    }
    //</editor-fold>

    //<editor-fold desc="Behaviors">
    override val behaviors: List<ItemBehavior>
        get() = schema.behaviors

    override fun <T : ItemBehavior> getBehavior(behaviorClass: KClass<T>): T {
        return getBehaviorOrNull(behaviorClass) ?: throw IllegalStateException("Item $key does not have a behavior of type ${behaviorClass.simpleName}")
    }
    //</editor-fold>
}

@JvmInline
internal value class PlayNekoStackImpl(
    override val itemStack: ItemStack,
) : BaseNekoStack, PlayNekoStack {
    companion object {
        private val ALL_FLAGS = ItemFlag.entries.toTypedArray()
    }

    constructor(mat: Material) : this(
        itemStack = ItemStack(mat), // strictly-Bukkit ItemStack
    )
    // FIXME remove it when the dedicated API is finished
    {
        RunningEnvironment.PRODUCTION.run { itemStack.addItemFlags(*ALL_FLAGS) }
    }

    override val tags: CompoundShadowTag
        get() {
            if (!isNmsBacked) {
                // If this is a strictly-Bukkit ItemStack,
                // the `wakame` compound should always be available (if not, create it)
                // as we need to create a NekoItem realization from an empty ItemStack.
                return itemStack.nekoCompound
            }
            // If this is a NMS-backed ItemStack,
            // reading/modifying is allowed only if it already has a `wakame` compound.
            // We explicitly prohibit modifying the ItemStacks, which are not already
            // NekoItem realization, in the world state because we want to avoid
            // undefined behaviors. Just imagine that a random code modifies a
            // vanilla item and make it an incomplete realization of NekoItem.
            return itemStack.nekoCompoundOrNull ?: throw NullPointerException("Can't read/modify the tags of NMS-backed ItemStack which is not NekoItem realization")
        }
}

@JvmInline
internal value class ShowNekoStackImpl(
    override val itemStack: ItemStack,
) : BaseNekoStack, ShowNekoStack {
    // The `wakame` compound should always be available (if not, create it)
    // as the ItemStack is solely used for the purpose of display, not for
    // the purpose of being used by players. Therefore, we can relax the
    // restrictions a little.
    override val tags: CompoundShadowTag
        get() = itemStack.nekoCompound

    override val customData: CustomDataAccessor
        get() = TODO("Not yet implemented")
}
