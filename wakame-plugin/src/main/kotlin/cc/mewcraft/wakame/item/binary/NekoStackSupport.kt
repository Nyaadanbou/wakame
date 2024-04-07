package cc.mewcraft.wakame.item.binary

import cc.mewcraft.wakame.NekoTags
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

internal interface BaseNekoStack : NekoStack {
    override val isNmsBacked: Boolean
        get() = itemStack.isNmsObjectBacked

    /**
     * Gets the "wakame" [CompoundTag][CompoundShadowTag] of this item.
     *
     * This **does not** include any other tags which are **not** part of the
     * wakame item NBT specifications, such as display name, lore, enchantment and
     * durability, which are already accessible via the Bukkit API. To get access to
     * those tags, just use the wrapped [itemStack].
     */
    val tags: CompoundShadowTag
        get() {
            if (!isNmsBacked) {
                // strictly-Bukkit ItemStack - the `wakame` compound is always available. If not, create one
                return itemStack.nekoCompound
            }
            // NMS-backed ItemStack - reading/modifying is allowed only if it already has a `wakame` compound
            return itemStack.nekoCompoundOrNull ?: throw NullPointerException("Can't read/modify the NBT of NMS-backed ItemStack which is not NekoStack")
        }

    override val isNeko: Boolean
        get() = itemStack.nekoCompoundOrNull != null

    override val isPlayStack: Boolean
        get() = !isShowStack // an NS is either PNS or SNS

    override val isShowStack: Boolean
        get() = tags.contains(NekoTags.Root.SHOW, ShadowTagType.BYTE)

    override val schema: NekoItem
        get() = NekoItemRegistry.INSTANCES[key]

    override val seed: Long
        get() = tags.getLong(NekoTags.Root.SEED)

    override val key: Key
        get() = Key(tags.getString(NekoTags.Root.KEY)) // TODO 分离 namespace 和 value

    override val variant: Int
        get() = tags.getInt(NekoTags.Root.SID)

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

    //<editor-fold desc="Setters">
    override fun erase() {
        itemStack.removeNekoCompound()
    }

    override fun putRoot(compoundTag: CompoundShadowTag) {
        itemStack.nekoCompound = compoundTag
    }

    override fun putSeed(seed: Long) {
        tags.putLong(NekoTags.Root.SEED, seed)
    }

    override fun putKey(key: Key) {
        tags.putString(NekoTags.Root.KEY, key.asString())
    }

    override fun putVariant(sid: Int) {
        tags.putInt(NekoTags.Root.SID, sid)
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
        if (!TestEnvironment.isRunningJUnit()) {
            itemStack.addItemFlags(*ALL_FLAGS)
        }
    }
}

@JvmInline
internal value class ShowNekoStackImpl(
    override val itemStack: ItemStack,
) : BaseNekoStack, ShowNekoStack {
    override val customData: CustomDataAccessor
        get() = TODO("Not yet implemented")
}
