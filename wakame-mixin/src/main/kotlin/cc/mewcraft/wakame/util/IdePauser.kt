package cc.mewcraft.wakame.util

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.SharedConstants
import java.time.Duration
import java.time.Instant
import java.util.function.Consumer

object IdePauser {

    @JvmStatic
    private var thePauser: Consumer<Throwable> = Consumer { throw it }

    @JvmStatic
    fun <T : Throwable> pauseInIde(t: T): T {
        if (SharedConstants.isRunningInIde) {
            LOGGER.error("Trying to throw a fatal exception, pausing in IDE", t)
            doPause(t)
        } else {
            LOGGER.warn("Suppressing a fatal exception since not in IDE", t)
        }
        return t
    }

    @JvmStatic
    private fun <T : Throwable> doPause(t: T) {
        val instant = Instant.now()
        LOGGER.warn("Did you remember to set a breakpoint here?")
        val bl = Duration.between(instant, Instant.now()).toMillis() > 500L
        if (!bl) {
            thePauser.accept(t)
        }
    }

    @JvmStatic
    fun setPause(missingBreakpointHandler: Consumer<Throwable>) {
        thePauser = missingBreakpointHandler
    }
}