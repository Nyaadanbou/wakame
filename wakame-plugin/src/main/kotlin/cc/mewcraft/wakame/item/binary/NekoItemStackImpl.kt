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
import cc.mewcraft.wakame.util.wakameCompound
import cc.mewcraft.wakame.util.wakameCompoundOrNull
import me.lucko.helper.shadows.nbt.CompoundShadowTag
import net.kyori.adventure.key.Key
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.koin.core.component.KoinComponent
import java.util.UUID

@OptIn(InternalApi::class)
internal class NekoItemStackImpl(
    override val handle: ItemStack,
    override val isOneOff: Boolean = false,
) : KoinComponent, NekoItemStack {
    constructor(mat: Material) : this(
        handle = ItemStack(mat) /* strictly-Bukkit ItemStack */,
        isOneOff = true /* so, it's a one-off instance */
    )

    // region WakaItemStack
    override val tags: CompoundShadowTag
        get() {
            if (isOneOff) {
                // strictly-Bukkit ItemStack - the `wakame` compound is always available. If not, create one
                return handle.wakameCompound
            }
            // NMS-backed ItemStack - reading/modifying is allowed only if it already has a `wakame` compound
            return checkNotNull(handle.wakameCompoundOrNull) { "Can't read/modify the NBT of NMS-backed ItemStack which is not WakaItemStack" }
        }

    override val isNeko: Boolean
        /*
        Implementation Notes:
          1) a one-off `this` is always considered Wakame item
          2) a NMS-backed `this` is considered Wakame item iff it has a `wakame` compound tag
        */
        get() = isOneOff || handle.wakameCompoundOrNull != null

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

    override val cellAccessor: CellAccessor by lazy(LazyThreadSafetyMode.NONE) {
        CellAccessorImpl(this)
    }

    override val metaAccessor: ItemMetaAccessor by lazy(LazyThreadSafetyMode.NONE) {
        ItemMetaAccessorImpl(this)
    }

    override val statsAccessor: ItemStatsAccessor by lazy(LazyThreadSafetyMode.NONE) {
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
    private fun edit(consumer: CompoundShadowTag.() -> Unit) {
        tags.consumer()
    }

    override fun putRoot(compoundTag: CompoundShadowTag) {
        handle.wakameCompound = compoundTag
    }

    override fun putKey(key: Key) = edit {
        putString(NekoTags.Root.NAMESPACE, key.namespace())
        putString(NekoTags.Root.ID, key.value())
    }

    override fun putNamespace(namespace: String) = edit {
        putString(NekoTags.Root.NAMESPACE, namespace)
    }

    override fun putId(id: String) = edit {
        putString(NekoTags.Root.ID, id)
    }
    // endregion

    // region BinaryCurseContext
    override val cellContext: CellAccessor
        get() = cellAccessor
    override val metaContext: ItemMetaAccessor
        get() = metaAccessor
    override val statsContext: ItemStatsAccessor
        get() = statsAccessor
    // endregion
}