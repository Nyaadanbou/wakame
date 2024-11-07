package cc.mewcraft.wakame.reforge.repair

import cc.mewcraft.wakame.reforge.common.PriceInstance
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component

interface RepairingTable {
    val id: String
    val enabled: Boolean
    val title: Component

    /**
     * 获取指定 [key] 对应的 [PriceInstance].
     *
     * 传入的 [key] 为物品的唯一标识.
     * 如果是原版物品, 则值应该为原版物品的命名空间路径, 例如: `minecraft:stone`.
     * 如果是萌芽物品, 则值应该为萌芽物品的命名空间路径, 例如: `material:tin_ingot`.
     *
     * @return 如果 [key] 对应的物品可以在本修复台进行修复, 则返回 [PriceInstance] 实例, 否则返回 `null`
     */
    fun getPrice(key: Key): PriceInstance?
}