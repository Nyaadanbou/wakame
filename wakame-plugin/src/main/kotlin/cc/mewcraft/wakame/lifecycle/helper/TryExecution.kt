@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.lifecycle.helper

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.lifecycle.LifecycleException
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.core.LoggerContext
import java.lang.reflect.InvocationTargetException

internal object TryExecution {

    /**
     * Wraps [run] in a try-catch block with error logging specific to lifecycle.
     * Returns whether the lifecycle has run successful, and also shuts down the server if it wasn't.
     */
    inline fun tryExecute(run: () -> Unit) {
        try {
            run()
        } catch (t: Throwable) {
            val cause = if (t is InvocationTargetException) t.targetException else t
            if (cause is LifecycleException) {
                LOGGER.error(cause.message)
            } else {
                LOGGER.error("An exception occurred during lifecycle execution", cause)
            }

            LOGGER.error("Lifecycle task failure")
            (LogManager.getContext(false) as LoggerContext).stop() // flush log messages
            Runtime.getRuntime().halt(-1) // force-quit process to prevent further errors
        }
    }

}