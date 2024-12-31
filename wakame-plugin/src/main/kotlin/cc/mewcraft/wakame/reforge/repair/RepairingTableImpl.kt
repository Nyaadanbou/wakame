package cc.mewcraft.wakame.reforge.repair

import cc.mewcraft.wakame.reforge.common.PriceInstance
import net.kyori.adventure.key.Key
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import net.kyori.examination.string.StringExaminer
import java.util.stream.Stream

internal data object WtfRepairingTable : RepairingTable {
    private val ZERO_PRICE_INSTANCE = PriceInstance(.0, emptyMap())

    override val id: String = "wtf"

    override fun getPrice(key: Key): PriceInstance {
        return ZERO_PRICE_INSTANCE
    }
}

internal class SimpleRepairingTable(
    override val id: String,
    private val items: Set<Key>,
) : RepairingTable, Examinable {

    override fun getPrice(key: Key): PriceInstance? {
        // 先看看传入的 key 参数所代表的物品是否可以在本修复台进行修复.
        if (key !in items) {
            return null
        }

        // 始终直接从 RepairingTableRegistry 获取 PriceInstance 实例.
        return RepairingTableRegistry.getItem(key)
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> {
        return Stream.of(
            ExaminableProperty.of("id", id),
            ExaminableProperty.of("items", items),
        )
    }

    override fun toString(): String {
        return StringExaminer.simpleEscaping().examine(this)
    }
}