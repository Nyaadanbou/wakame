package cc.mewcraft.wakame.item.binary

import cc.mewcraft.wakame.NekoTags
import cc.mewcraft.wakame.item.EffectiveSlot
import cc.mewcraft.wakame.item.binary.cell.ItemCellAccessor
import cc.mewcraft.wakame.item.binary.cell.ItemCellAccessorImpl
import cc.mewcraft.wakame.item.binary.meta.ItemMetaAccessor
import cc.mewcraft.wakame.item.binary.meta.ItemMetaAccessorImpl
import cc.mewcraft.wakame.item.binary.stats.ItemStatisticsAccessor
import cc.mewcraft.wakame.item.binary.stats.ItemStatisticsAccessorImpl
import cc.mewcraft.wakame.item.schema.NekoItem
import cc.mewcraft.wakame.item.schema.behavior.ItemBehavior
import cc.mewcraft.wakame.registry.NekoItemRegistry
import cc.mewcraft.wakame.util.*
import me.lucko.helper.shadows.nbt.CompoundShadowTag
import net.kyori.adventure.key.Key
import org.bukkit.Material
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.koin.core.component.KoinComponent
import java.util.UUID
import kotlin.reflect.KClass

@JvmInline
internal value class NekoStackImpl(
    override val handle: ItemStack,
) : KoinComponent, NekoStack {

    // The second constructor is used to create the impl from org.bukkit.Material
    constructor(mat: Material) : this(
        handle = ItemStack(mat), /* strictly-Bukkit ItemStack */
    ) {
        // FIXME remove it when the dedicated API is finished
        if (!TestEnvironment.isRunningJUnit()) {
            handle.addItemFlags(*ItemFlag.entries.toTypedArray())
        }
    }

    override val isNmsBacked: Boolean
        get() = handle.isNmsObjectBacked

    /**
     * Gets the "wakame" [CompoundTag][CompoundShadowTag] of this item.
     *
     * This **does not** include any other tags which are **not** part of the
     * wakame item NBT specifications, such as display name, lore, enchantment and
     * durability, which are already accessible via Server API. To get access to
     * these tags, just use the wrapped [handle].
     */
    internal val tags: CompoundShadowTag
        get() {
            if (!isNmsBacked) {
                // strictly-Bukkit ItemStack - the `wakame` compound is always available. If not, create one
                return handle.nekoCompound
            }
            // NMS-backed ItemStack - reading/modifying is allowed only if it already has a `wakame` compound
            return handle.nekoCompoundOrNull ?: throw NullPointerException("Can't read/modify the NBT of NMS-backed ItemStack which is not NekoStack")
        }

    override val isNeko: Boolean
        get() = handle.nekoCompoundOrNull != null

    override val isNotNeko: Boolean
        get() = !isNeko

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
        handle.removeNekoCompound()
    }

    override fun putRoot(compoundTag: CompoundShadowTag) {
        handle.nekoCompound = compoundTag
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