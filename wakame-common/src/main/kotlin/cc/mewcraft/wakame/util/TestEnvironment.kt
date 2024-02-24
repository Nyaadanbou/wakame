package cc.mewcraft.wakame.util

object TestEnvironment {

    private val cache: Boolean by lazy(LazyThreadSafetyMode.NONE) {
        listOf(
            "org.junit.Test",
            "org.junit.jupiter.api.Test"
        ).map {
            runCatching { Class.forName(it) }
        }.any { it.isSuccess }
    }

    /**
     * Returns `true` if we are in any JUnit environment.
     */
    fun isRunningJUnit(): Boolean = cache

}