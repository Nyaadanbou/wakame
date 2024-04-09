package cc.mewcraft.wakame.util

enum class RunningEnvironment {
    TEST {
        override fun isRunning(): Boolean {
            return runningJunitTest
        }
    },
    PRODUCTION {
        override fun isRunning(): Boolean {
            return !runningJunitTest
        }
    };

    /**
     * Runs the code block only if we are in this environment.
     */
    fun run(block: () -> Unit) {
        if (isRunning()) block()
    }

    /**
     * Returns `true` if we are in this environment.
     */
    abstract fun isRunning(): Boolean

    /**
     * A cache which keeps the current environment information.
     */
    protected val runningJunitTest: Boolean by lazy {
        listOf(
            "org.junit.Test",
            "org.junit.jupiter.api.Test"
        ).map {
            runCatching { Class.forName(it) }
        }.any { it.isSuccess }
    }
}
