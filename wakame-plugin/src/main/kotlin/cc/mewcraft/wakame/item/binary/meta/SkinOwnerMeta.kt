package cc.mewcraft.wakame.item.binary.meta

import cc.mewcraft.wakame.item.ItemMetaKeys
import net.kyori.adventure.key.Key
import java.util.UUID

/**
 * 物品的皮肤的所有者。
 */
@JvmInline
value class BSkinOwnerMeta(
    private val accessor: ItemMetaAccessor,
) : BinaryItemMeta<UUID> {
    override val key: Key
        get() = ItemMetaKeys.SKIN_OWNER
    override val exists: Boolean
        get() = accessor.rootOrNull?.contains(ItemMetaKeys.SKIN_OWNER.value()) ?: false

    override fun getOrNull(): UUID? {
        val rootOrNull = accessor.rootOrNull
        if (rootOrNull == null || !rootOrNull.hasUUID(key.value()))
            return null
        return rootOrNull.getUUID(key.value())
    }

    override fun remove() {
        accessor.rootOrNull?.remove(key.value())
    }

    override fun set(value: UUID) {
        accessor.rootOrCreate.putUUID(key.value(), value)
    }
}