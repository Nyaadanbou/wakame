package cc.mewcraft.wakame.item.component

import cc.mewcraft.wakame.config.*
import cc.mewcraft.wakame.display.RENDERERS_CONFIG_DIR
import cc.mewcraft.wakame.display2.RendererSystemName
import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.registry.*
import org.koin.core.component.KoinComponent

/**
 * 物品组件相关的注册表.
 */
internal object ItemComponentRegistry : KoinComponent, Initializable {

    const val NODE_COMPONENTS = "components"
    const val RENDERER_SYSTEM_DESCRIPTOR_FILE = "$RENDERERS_CONFIG_DIR/<system>/descriptors.yml"

    /**
     * 物品组件的全局配置文件.
     */
    internal val CONFIG = Configs.YAML[ITEM_GLOBAL_CONFIG_FILE].derive(NODE_COMPONENTS)

    /**
     * 物品组件类型的注册表.
     */
    internal val TYPES: Registry<String, ItemComponentType<*>> = SimpleRegistry()

    fun getDescriptorsByRendererSystemName(name: RendererSystemName): ConfigProvider {
        val path = RENDERER_SYSTEM_DESCRIPTOR_FILE.replace("<system>", name.name.lowercase())
        return Configs.YAML[path].derive(NODE_COMPONENTS)
    }
}