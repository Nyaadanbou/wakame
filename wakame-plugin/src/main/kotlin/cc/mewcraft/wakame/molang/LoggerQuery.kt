package cc.mewcraft.wakame.molang

import cc.mewcraft.wakame.LOGGER
import team.unnamed.mocha.runtime.binding.Binding

@Binding(
    value = ["logger", "log"],
)
object LoggerQuery {

    /**
     * logger.info('Hello, World!')
     */
    @Binding("info")
    @JvmStatic
    fun info(str: String) {
        LOGGER.info(str)
    }

    /**
     * logger.warn('Hello, World!')
     */
    @Binding("warn")
    @JvmStatic
    fun warn(str: String) {
        LOGGER.warn(str)
    }

    /**
     * logger.error('Hello, World!')
     */
    @Binding("error")
    @JvmStatic
    fun error(str: String) {
        LOGGER.error(str)
    }
}