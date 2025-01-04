package cc.mewcraft.wakame

import java.time.Duration
import java.time.Instant
import java.util.function.Consumer

object Util {
    private var thePauser: Consumer<String?> = Consumer { message: String -> doPause(message) }

    init {
        setPause(LOGGER::error) // 简单记录一下
    }

    @JvmStatic
    fun <T : Throwable> pauseInIde(t: T): T {
        if (SharedConstants.IS_RUNNING_IN_IDE) {
            LOGGER.error("Trying to throw a fatal exception, pausing in IDE", t)
            doPause(t.message)
        }
        return t
    }

    @JvmStatic
    private fun doPause(message: String?) {
        val instant = Instant.now()
        LOGGER.warn("Did you remember to set a breakpoint here?")
        val bl = Duration.between(instant, Instant.now()).toMillis() > 500L
        if (!bl) {
            thePauser.accept(message)
        }
    }

    @JvmStatic
    fun setPause(missingBreakpointHandler: Consumer<String?>) {
        thePauser = missingBreakpointHandler
    }
}