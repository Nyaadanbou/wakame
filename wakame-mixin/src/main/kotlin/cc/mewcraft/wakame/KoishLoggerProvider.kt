package cc.mewcraft.wakame

import net.kyori.adventure.text.logger.slf4j.ComponentLogger
import kotlin.reflect.KProperty

@get:JvmName("getLogger")
val LOGGER: ComponentLogger by KoishLoggerProvider

// 为了让代码在单元测试环境里也能直接使用 LOGGER, 我们创建该容器来装载不同环境下的 Logger 实例
object KoishLoggerProvider {
    private var LOGGER: ComponentLogger? = null

    init {
        if (SharedConstants.isRunningInIde) {
            // 单元测试使用专门的 Logger, 服务端上则由 PluginBootstrap#bootstrap 分配
            set(ComponentLogger.logger("KoishTest"))
        }
    }

    fun set(logger: ComponentLogger) {
        LOGGER = logger
    }

    fun get(): ComponentLogger {
        return LOGGER ?: error("Koish logger not initialized!")
    }

    operator fun getValue(thisRef: Any?, property: KProperty<*>?): ComponentLogger = get()
}