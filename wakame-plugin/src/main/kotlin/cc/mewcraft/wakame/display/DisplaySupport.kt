package cc.mewcraft.wakame.display

import cc.mewcraft.wakame.config.Configs
import net.kyori.adventure.text.minimessage.MiniMessage
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.Logger

/**
 * The singleton holds the common properties and functions to
 * write the code in this package.
 */
internal object DisplaySupport : KoinComponent {
    val LOGGER by inject<Logger>()

    // config
    const val RENDERER_LAYOUT_LINE_PATTERN = "\\((.+?)\\)(.*)"
    const val RENDERER_CONFIG_LAYOUT_NODE_NAME = "renderer_layout"
    val RENDERER_CONFIG_PROVIDER by lazy { Configs.YAML[RENDERER_CONFIG_FILE] }
    val DYNAMIC_LORE_META_CREATOR_REGISTRY by inject<DynamicLoreMetaCreatorRegistry>()

    // mini message
    val MINI: MiniMessage by inject()
}