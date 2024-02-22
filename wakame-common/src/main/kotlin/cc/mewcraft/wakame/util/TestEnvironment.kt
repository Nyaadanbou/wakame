package cc.mewcraft.wakame.util

object TestEnvironment {

    /**
     * Returns `true` if we are in any JUnit environment.
     */
    fun isRunningJUnit(): Boolean =
        listOf(
            "org.junit.Test",
            "org.junit.jupiter.api.Test"
        ).map {
            runCatching { Class.forName(it) }
        }.any { it.isSuccess }

}