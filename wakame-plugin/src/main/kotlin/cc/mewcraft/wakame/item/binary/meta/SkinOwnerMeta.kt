package cc.mewcraft.wakame.item.binary.meta

import cc.mewcraft.wakame.display.LoreLine
import cc.mewcraft.wakame.item.ItemMetaConstants
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import java.util.UUID

/**
 * 物品的皮肤的所有者。
 */
@JvmInline
value class BSkinOwnerMeta(
    private val accessor: ItemMetaAccessor,
) : BinaryItemMeta<UUID> {
    override val key: Key
        get() = ItemMetaConstants.createKey { SKIN_OWNER }
    override val exists: Boolean
        get() = accessor.rootOrNull?.contains(ItemMetaConstants.SKIN_OWNER) ?: false

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

    override fun provideDisplayLore(): LoreLine {
        val skinOwner = get()
        val key = ItemMetaSupport.getLineKey(this) ?: return LoreLine.noop()
        val lines = ItemMetaSupport.mini().deserialize(tooltips.single, Placeholder.component("value", Component.text(skinOwner.toString())))
        return LoreLine.simple(key, listOf(lines))
    }

    private companion object : ItemMetaConfig(
        ItemMetaConstants.SKIN_OWNER
    ) {
        val tooltips: SingleTooltips = SingleTooltips()
    }
}