package cc.mewcraft.wakame.item.binary

import cc.mewcraft.wakame.NekoTags
import cc.mewcraft.wakame.annotation.InternalApi
import cc.mewcraft.wakame.item.binary.cell.CellAccessor
import cc.mewcraft.wakame.item.binary.cell.CellAccessorImpl
import cc.mewcraft.wakame.item.binary.meta.ItemMetaAccessor
import cc.mewcraft.wakame.item.binary.meta.ItemMetaAccessorImpl
import cc.mewcraft.wakame.item.binary.stats.ItemStatsAccessor
import cc.mewcraft.wakame.item.binary.stats.ItemStatsAccessorImpl
import cc.mewcraft.wakame.item.scheme.NekoItem
import cc.mewcraft.wakame.registry.NekoItemRegistry
import cc.mewcraft.wakame.util.nekoCompound
import cc.mewcraft.wakame.util.nekoCompoundOrNull
import cc.mewcraft.wakame.util.removeNekoCompound
import me.lucko.helper.shadows.nbt.CompoundShadowTag
import net.kyori.adventure.key.Key
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.koin.core.component.KoinComponent
import java.util.UUID

internal class NekoItemStackImpl(
    override val handle: ItemStack,
    override val isOneOff: Boolean = false,
) : KoinComponent, NekoItemStack {
    constructor(mat: Material) : this(
        handle = ItemStack(mat) /* strictly-Bukkit ItemStack */,
        isOneOff = true /* so, it's a one-off instance */
    )

    @InternalApi
    override fun erase() {
        handle.removeNekoCompound()
    }

    // region WakaItemStack
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
            return checkNotNull(handle.nekoCompoundOrNull) { "Can't read/modify the NBT of NMS-backed ItemStack which is not WakaItemStack" }
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
        get() = NekoItemRegistry.get(key)
            ?: throw NullPointerException()

    override val namespace: String
        get() = tags.getString(NekoTags.Root.NAMESPACE)

    override val id: String
        get() = tags.getString(NekoTags.Root.ID)

    override val key: Key
        get() = Key.key(namespace, id)

    override val uuid: UUID
        get() = NekoItemRegistry.get(key)?.uuid
            ?: throw NullPointerException()

    override val cells: CellAccessor by lazy(LazyThreadSafetyMode.NONE) {
        CellAccessorImpl(this)
    }

    override val metadata: ItemMetaAccessor by lazy(LazyThreadSafetyMode.NONE) {
        ItemMetaAccessorImpl(this)
    }

    override val statistics: ItemStatsAccessor by lazy(LazyThreadSafetyMode.NONE) {
        ItemStatsAccessorImpl(this)
    }

    override fun hashCode(): Int {
        return handle.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return handle == other
    }
    // endregion

    // region WakaItemStackSetter
    override fun putRoot(compoundTag: CompoundShadowTag) {
        handle.nekoCompound = compoundTag
    }

    override fun putKey(key: Key) {
        tags.putString(NekoTags.Root.NAMESPACE, key.namespace())
        tags.putString(NekoTags.Root.ID, key.value())
    }

    override fun putNamespace(namespace: String) {
        tags.putString(NekoTags.Root.NAMESPACE, namespace)
    }

    override fun putId(id: String) {
        tags.putString(NekoTags.Root.ID, id)
    }
    // endregion

}