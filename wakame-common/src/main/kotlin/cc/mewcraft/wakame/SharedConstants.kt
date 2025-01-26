package cc.mewcraft.wakame

/**
 * 适用于整个萌芽项目的常量.
 */
object SharedConstants {
    const val PLUGIN_NAME = "wakame"
    const val PLUGIN_NAME_2 = "koish"
    const val ROOT_NBT_NAME = "wakame"

    /**
     * 是否开启调试模式.
     */
    val isDebug = true

    /**
     * 检查 JVM 是否在 IDE 中运行.
     */
    val isRunningInIde = listOf(
        "org.junit.Test", // JUnit 4
        "org.junit.jupiter.api.Test" // JUnit 5
    ).map {
        runCatching { Class.forName(it) }
    }.any { it.isSuccess }
}