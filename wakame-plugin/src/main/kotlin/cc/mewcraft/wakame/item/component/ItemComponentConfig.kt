package cc.mewcraft.wakame.item.component

import cc.mewcraft.wakame.config.entry
import cc.mewcraft.wakame.config.node
import net.kyori.adventure.text.Component
import java.util.concurrent.ConcurrentHashMap

/**
 * 封装了一个物品组件的配置文件, 以便用户对一个组件(在全局上)具有一定的控制权.
 *
 * @param id 该组件在配置文件中的路径
 */
internal class ItemComponentConfig
private constructor(
    private val id: String,
) {

    companion object {
        private val INSTANCES = ConcurrentHashMap<String, ItemComponentConfig>()

        /**
         * 获取指定 [id] 对应的 [ItemComponentConfig] 实例.
         * 使用同一个 [id] 多次调用本函数返回的都是同一个实例.
         *
         * 一般来说, [id] 的取值应该来自 [cc.mewcraft.wakame.item.ItemConstants]
         *
         * @param id 该组件在配置文件中的路径
         */
        fun provide(id: String): ItemComponentConfig {
            return INSTANCES.computeIfAbsent(id, ::ItemComponentConfig)
        }
    }

    /**
     * 根配置文件.
     */
    val rootNode = ItemComponentRegistry.CONFIG.node(id)

    /**
     * 该组件是否启用? (具体的作用之后再逐渐完善)
     */
    val enabled by rootNode.entry<Boolean>("enabled")

    /**
     * 该组件的显示名字.
     */
    val displayName by rootNode.entry<Component>("display_name")
}