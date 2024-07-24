package cc.mewcraft.wakame.registry

import cc.mewcraft.wakame.config.Configs
import cc.mewcraft.wakame.config.derive
import cc.mewcraft.wakame.initializer.Initializable
import org.koin.core.component.KoinComponent

/**
 * 诅咒相关的注册表.
 */
object CurseRegistry : KoinComponent, Initializable {
    const val NODE_CURSES = "curses"

    /**
     * 诅咒的全局配置文件.
     */
    internal val CONFIG = Configs.YAML[CURSE_GLOBAL_CONFIG_FILE].derive(NODE_CURSES)
}