package cc.mewcraft.wakame.item.binary.meta

import cc.mewcraft.wakame.item.ItemMetaKeys
import me.lucko.helper.shadows.nbt.CompoundShadowTag
import net.kyori.adventure.key.Key
import java.util.UUID

/**
 * 物品的皮肤的所有者。
 */
internal class SkinOwnerMeta(
    private val holder: ItemMetaHolderImpl,
) : BinaryItemMeta<UUID> {
    override val key: Key = ItemMetaKeys.SKIN_OWNER

    override fun getOrNull(): UUID? {
        val rootOrNull = holder.rootOrNull
        if (rootOrNull == null || !rootOrNull.hasUUID(key.value()))
            return null
        return rootOrNull.getUUID(key.value())
    }

    override fun remove() {
        holder.rootOrNull?.remove(key.value())
    }

    override fun set(value: UUID) {
        holder.rootOrCreate.putUUID(key.value(), value)
    }

    companion object : ItemMetaCompanion {
        override fun contains(compound: CompoundShadowTag): Boolean {
            return compound.contains(ItemMetaKeys.SKIN_OWNER.value())
        }
    }
}