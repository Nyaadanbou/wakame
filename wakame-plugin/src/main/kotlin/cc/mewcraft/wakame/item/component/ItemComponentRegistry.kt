package cc.mewcraft.wakame.item.component

import cc.mewcraft.wakame.config.Configs
import org.koin.core.component.KoinComponent

/**
 * 物品组件相关的注册表.
 */
internal object ItemComponentRegistry : KoinComponent {

    const val CONFIG_FILE_NAME = "items.yml"
    const val NODE_COMPONENTS = "components"

    /**
     * 物品组件的全局配置文件.
     */
    internal val CONFIG = Configs.YAML[CONFIG_FILE_NAME].node(NODE_COMPONENTS)

    /**
     * 物品组件类型的注册表.
     */
    internal val TYPES = HashMap<String, ItemComponentType<*>>()
}