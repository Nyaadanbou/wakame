package cc.mewcraft.wakame.item.component

import cc.mewcraft.wakame.config.ConfigAccess
import cc.mewcraft.wakame.config.node

/**
 * 物品组件相关的注册表.
 */
internal object ItemComponentRegistry {

    const val CONFIG_ID = "items"
    const val NODE_COMPONENTS = "components"

    /**
     * 物品组件的全局配置文件.
     */
    internal val CONFIG = ConfigAccess.INSTANCE[CONFIG_ID].node(NODE_COMPONENTS)

    /**
     * 物品组件类型的注册表.
     */
    internal val TYPES = HashMap<String, ItemComponentType<*>>()
}