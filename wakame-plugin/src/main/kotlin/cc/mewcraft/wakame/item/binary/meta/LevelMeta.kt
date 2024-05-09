package cc.mewcraft.wakame.item.binary.meta

import cc.mewcraft.wakame.display.LoreLine
import cc.mewcraft.wakame.item.ItemMetaConstants
import cc.mewcraft.wakame.util.getIntOrNull
import cc.mewcraft.wakame.util.toStableByte
import me.lucko.helper.nbt.ShadowTagType
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder

/**
 * 物品的等级。不是所有物品都有等级，因此可能为空。
 */
@JvmInline
value class BLevelMeta(
    private val accessor: ItemMetaAccessor,
) : BinaryItemMeta<Int> {
    override val key: Key
        get() = ItemMetaConstants.createKey { LEVEL }
    override val exists: Boolean
        get() = accessor.rootOrNull?.contains(ItemMetaConstants.LEVEL, ShadowTagType.BYTE) ?: false

    override fun getOrNull(): Int? {
        return accessor.rootOrNull?.getIntOrNull(key.value())
    }

    override fun remove() {
        accessor.rootOrNull?.remove(key.value())
    }

    override fun set(value: Int) {
        require(value >= 0) { "level >= 0" }
        accessor.rootOrCreate.putByte(key.value(), value.toStableByte())
    }

    override fun provideDisplayLore(): LoreLine {
        val level = get()
        val key = Implementations.getLineKey(this)
        val lines = Implementations.mini().deserialize(tooltips.single, Placeholder.component("value", Component.text(level)))
        return ItemMetaLoreLine(key, listOf(lines))
    }

    private companion object : ItemMetaConfig(
        ItemMetaConstants.LEVEL
    ) {
        val tooltips: SingleTooltips = SingleTooltips()
    }
}

fun BLevelMeta?.getOrDefault(): Int {
    return this?.getOrNull() ?: 0
}