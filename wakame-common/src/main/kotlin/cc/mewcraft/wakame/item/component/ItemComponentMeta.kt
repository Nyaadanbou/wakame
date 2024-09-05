package cc.mewcraft.wakame.item.component

import net.kyori.adventure.key.Key

/**
 * 代表一个物品组件(wakame)的元数据.
 *
 * 元数据指的是跟物品组件本身相关的数据, 这些数据不随着世界状态的变化而变化.
 */
interface ItemComponentMeta {
    /**
     * 组件的配置文件的路径.
     */
    val configPath: String

    /**
     * 组件在提示框里的索引.
     */
    val tooltipKey: Key
}