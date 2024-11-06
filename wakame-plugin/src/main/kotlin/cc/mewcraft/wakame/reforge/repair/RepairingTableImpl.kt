package cc.mewcraft.wakame.reforge.repair

import cc.mewcraft.wakame.reforge.common.PriceInstance
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.*

internal object WtfRepairingTable : RepairingTable {
    private val ZERO_PRICE_INSTANCE = PriceInstance(.0, emptyMap())

    override val id: String = "wtf"
    override val enabled: Boolean = true
    override val title: Component = text("Repairing Station (Cheat ON)")

    override fun getPrice(key: Key): PriceInstance {
        return ZERO_PRICE_INSTANCE
    }
}

internal class SimpleRepairingTable(
    override val id: String,
    override val enabled: Boolean,
    override val title: Component,
    private val items: Set<Key>,
) : RepairingTable {

    override fun getPrice(key: Key): PriceInstance? {
        // 先看看传入的 key 参数所代表的物品是否可以在本修复台进行修复.
        if (key !in items) {
            return null
        }

        // 始终直接从 RepairingTableRegistry 获取 PriceInstance 实例.
        return RepairingTableRegistry.getItem(key)
    }
}