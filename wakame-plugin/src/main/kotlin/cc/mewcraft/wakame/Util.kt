package cc.mewcraft.wakame

import java.time.Duration
import java.time.Instant
import java.util.function.Consumer

object Util {
    private var thePauser: Consumer<Throwable> = Consumer { throw it }

    fun <T : Throwable> pauseInIde(t: T): T {
        if (SharedConstants.IS_RUNNING_IN_IDE) {
            LOGGER.error("Trying to throw a fatal exception, pausing in IDE", t)
            doPause(t)
        } else {
            LOGGER.warn("A fatal exception occurred, but suppressed since not in IDE", t)
        }
        return t
    }

    private fun <T : Throwable> doPause(t: T) {
        val instant = Instant.now()
        LOGGER.warn("Did you remember to set a breakpoint here?")
        val bl = Duration.between(instant, Instant.now()).toMillis() > 500L
        if (!bl) {
            thePauser.accept(t)
        }
    }

    fun setPause(missingBreakpointHandler: Consumer<Throwable>) {
        thePauser = missingBreakpointHandler
    }
}