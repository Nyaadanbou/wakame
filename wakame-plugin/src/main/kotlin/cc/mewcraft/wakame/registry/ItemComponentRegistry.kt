package cc.mewcraft.wakame.registry

import cc.mewcraft.wakame.config.Configs
import cc.mewcraft.wakame.config.derive
import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.item.component.ItemComponentType
import org.koin.core.component.KoinComponent

/**
 * 物品组件相关的注册表.
 */
object ItemComponentRegistry : KoinComponent, Initializable {

    internal val CONFIG by lazy { Configs.YAML[ITEM_GLOBAL_CONFIG_FILE].derive("components") }

    /**
     * 类型的注册表.
     */
    val TYPES: Registry<String, ItemComponentType<*>> = SimpleRegistry()

    override fun onPreWorld() {

    }
}