package cc.mewcraft.wakame

/**
 * 适用于整个萌芽项目的常量.
 */
object SharedConstants {
    const val PLUGIN_NAME = "wakame"
    const val ROOT_NBT_NAME = "wakame"

    /**
     * 检查 JVM 是否在 IDE 中运行.
     */
    var IS_RUNNING_IN_IDE = false

    // 赋值 IS_RUNNING_IN_IDE
    init {
        IS_RUNNING_IN_IDE = listOf(
            "org.junit.Test", // JUnit 4
            "org.junit.jupiter.api.Test" // JUnit 5
        ).map {
            runCatching { Class.forName(it) }
        }.any { it.isSuccess }
    }
}