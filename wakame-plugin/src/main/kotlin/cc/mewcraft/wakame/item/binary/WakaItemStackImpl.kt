package cc.mewcraft.wakame.item.binary

import cc.mewcraft.wakame.item.binary.cell.CellAccessor
import cc.mewcraft.wakame.item.binary.cell.CellAccessorImpl
import cc.mewcraft.wakame.item.binary.meta.ItemMetaAccessor
import cc.mewcraft.wakame.item.binary.meta.ItemMetaAccessorImpl
import cc.mewcraft.wakame.item.binary.stats.ItemStatsAccessor
import cc.mewcraft.wakame.item.binary.stats.ItemStatsAccessorImpl
import cc.mewcraft.wakame.item.scheme.WakaItem
import cc.mewcraft.wakame.registry.NekoItemRegistry
import cc.mewcraft.wakame.util.wakameCompound
import cc.mewcraft.wakame.util.wakameCompoundOrNull
import me.lucko.helper.shadows.nbt.CompoundShadowTag
import net.kyori.adventure.key.Key
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.UUID

internal class WakaItemStackImpl(
    override val handle: ItemStack,
    override val isOneOff: Boolean = false,
) : KoinComponent, WakaItemStack {
    private val itemRegistry: NekoItemRegistry by inject()

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

    override val isWakame: Boolean
        /*
        Implementation Notes:
          1) a one-off `this` is always considered Wakame item
          2) a NMS-backed `this` is considered Wakame item iff it has a `wakame` compound tag
        */
        get() = isOneOff || handle.wakameCompoundOrNull != null

    override val isNotWakame: Boolean
        get() = !isWakame

    override val scheme: WakaItem
        get() = itemRegistry.get(key)
            ?: throw NullPointerException()

    override val namespace: String
        get() = tags.getString(WakaItemStackTagNames.NAMESPACE)

    override val id: String
        get() = tags.getString(WakaItemStackTagNames.ID)

    override val key: Key
        get() = Key.key(namespace, id)

    override val uuid: UUID
        get() = itemRegistry.get(key)?.uuid
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
        putString(WakaItemStackTagNames.NAMESPACE, key.namespace())
        putString(WakaItemStackTagNames.ID, key.value())
    }

    override fun putNamespace(namespace: String) = edit {
        putString(WakaItemStackTagNames.NAMESPACE, namespace)
    }

    override fun putPath(id: String) = edit {
        putString(WakaItemStackTagNames.ID, id)
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