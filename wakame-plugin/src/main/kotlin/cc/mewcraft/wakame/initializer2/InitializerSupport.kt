@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.initializer2

import cc.mewcraft.wakame.LOGGER
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.core.LoggerContext
import java.lang.reflect.InvocationTargetException

internal object InitializerSupport {
    /**
     * Wraps [run] in a try-catch block with error logging specific to initialization.
     * Returns whether the initialization was successful, and also shuts down the server if it wasn't.
     */
    inline fun tryInit(run: () -> Unit) {
        try {
            run()
        } catch (t: Throwable) {
            val cause = if (t is InvocationTargetException) t.targetException else t
            if (cause is InitializationException) {
                LOGGER.error(cause.message)
            } else {
                LOGGER.error("An exception occurred during initialization", cause)
            }

            LOGGER.error("Initialization failure")
            (LogManager.getContext(false) as LoggerContext).stop() // flush log messages
            Runtime.getRuntime().halt(-1) // force-quit process to prevent further errors
        }
    }
}