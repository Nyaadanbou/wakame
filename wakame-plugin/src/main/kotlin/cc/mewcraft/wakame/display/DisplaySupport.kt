package cc.mewcraft.wakame.display

import cc.mewcraft.wakame.config.Configs

/**
 * The singleton holds the common properties and functions to
 * write the code in this package.
 */
internal object DisplaySupport {
    // 配置文件的常量
    const val RENDERER_CONFIG_LAYOUT_NODE_KEY = "renderer_layout"

    // 全局渲染配置文件的 ConfigProvider
    val RENDERER_GLOBAL_CONFIG_PROVIDER by lazy { Configs.YAML[RENDERER_GLOBAL_CONFIG_FILE] }
}