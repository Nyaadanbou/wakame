package cc.mewcraft.wakame.molang

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.Logger
import team.unnamed.mocha.runtime.binding.Binding

@Binding(
    value = ["logger", "log"],
)
object LoggerQuery : KoinComponent {
    private val logger: Logger by inject()

    /**
     * logger.info('Hello, World!')
     */
    @Binding("info")
    @JvmStatic
    fun info(str: String) {
        logger.info(str)
    }

    /**
     * logger.warn('Hello, World!')
     */
    @Binding("warn")
    @JvmStatic
    fun warn(str: String) {
        logger.warn(str)
    }

    /**
     * logger.error('Hello, World!')
     */
    @Binding("error")
    @JvmStatic
    fun error(str: String) {
        logger.error(str)
    }
}