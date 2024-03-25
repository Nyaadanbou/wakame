package cc.mewcraft.wakame.item.binary

import cc.mewcraft.wakame.NekoTags
import cc.mewcraft.wakame.item.EffectiveSlot
import cc.mewcraft.wakame.item.binary.cell.ItemCellHolder
import cc.mewcraft.wakame.item.binary.cell.ItemCellHolderImpl
import cc.mewcraft.wakame.item.binary.meta.ItemMetaHolder
import cc.mewcraft.wakame.item.binary.meta.ItemMetaHolderImpl
import cc.mewcraft.wakame.item.binary.stats.ItemStatisticsHolder
import cc.mewcraft.wakame.item.binary.stats.ItemStatisticsHolderImpl
import cc.mewcraft.wakame.item.scheme.NekoItem
import cc.mewcraft.wakame.registry.NekoItemRegistry
import cc.mewcraft.wakame.util.TestEnvironment
import cc.mewcraft.wakame.util.nekoCompound
import cc.mewcraft.wakame.util.nekoCompoundOrNull
import cc.mewcraft.wakame.util.removeNekoCompound
import me.lucko.helper.shadows.nbt.CompoundShadowTag
import net.kyori.adventure.key.Key
import org.bukkit.Material
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.koin.core.component.KoinComponent
import java.util.UUID

internal class NekoStackImpl(
    override val handle: ItemStack,
    override val isOneOff: Boolean = false,
) : KoinComponent, NekoStack {
    constructor(mat: Material) : this(
        handle = ItemStack(mat) /* strictly-Bukkit ItemStack */,
        isOneOff = true /* so, it's a one-off instance */
    ) {
        // FIXME remove it when the dedicated API is finished
        if (!TestEnvironment.isRunningJUnit()) {
            handle.addItemFlags(*ItemFlag.entries.toTypedArray())
        }
    }

    /**
     * The "wakame" [CompoundTag][CompoundShadowTag] of this item.
     *
     * This **does not** include any other tags which are **not** part of the
     * wakame item NBT specifications, such as display name, enchantment and
     * durability, which are already accessible via Paper API. To get access to
     * these tags, just use the wrapped [handle].
     */
    internal val tags: CompoundShadowTag
        get() {
            if (isOneOff) {
                // strictly-Bukkit ItemStack - the `wakame` compound is always available. If not, create one
                return handle.nekoCompound
            }
            // NMS-backed ItemStack - reading/modifying is allowed only if it already has a `wakame` compound
            return checkNotNull(handle.nekoCompoundOrNull) { "Can't read/modify the NBT of NMS-backed ItemStack which is not NekoStack" }
        }

    override val isNeko: Boolean
        /*
        Implementation Notes:
          1) a one-off `this` is always considered Wakame item
          2) a NMS-backed `this` is considered Wakame item iff it has a `wakame` compound tag
        */
        get() = isOneOff || handle.nekoCompoundOrNull != null

    override val isNotNeko: Boolean
        get() = !isNeko

    override val scheme: NekoItem
        get() = NekoItemRegistry.INSTANCES.get(key)

    override val seed: Long
        get() = tags.getLong(NekoTags.Root.SEED)

    override val key: Key
        get() = Key.key(tags.getString(NekoTags.Root.KEY))

    override val variant: Int
        get() = tags.getInt(NekoTags.Root.SID)

    override val uuid: UUID
        get() = NekoItemRegistry.INSTANCES.get(key).uuid

    override val effectiveSlot: EffectiveSlot
        get() = NekoItemRegistry.INSTANCES.get(key).effectiveSlot

    override val cell: ItemCellHolder = ItemCellHolderImpl(this)

    override val meta: ItemMetaHolder = ItemMetaHolderImpl(this)

    override val statistics: ItemStatisticsHolder = ItemStatisticsHolderImpl(this)

    override fun hashCode(): Int {
        return handle.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return handle == other
    }

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

}