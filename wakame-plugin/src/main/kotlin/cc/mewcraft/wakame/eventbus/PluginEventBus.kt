package cc.mewcraft.wakame.eventbus

import cc.mewcraft.wakame.NEKO_PLUGIN
import cc.mewcraft.wakame.util.RunningEnvironment
import com.github.shynixn.mccoroutine.bukkit.scope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.plus

internal object PluginEventBus {
    private val eventBus: EventBus = run {
        when {
            // 如果是测试环境, 则只能用自定义的 coroutine scope
            RunningEnvironment.TEST.isRunning() -> {
                EventBus(CoroutineScope(Dispatchers.Default) + SupervisorJob())
            }

            // 如果是生产环境, 则使用插件的 coroutine scope
            RunningEnvironment.PRODUCTION.isRunning() -> {
                EventBus(NEKO_PLUGIN.scope)
            }

            else -> {
                throw IllegalStateException("Should never happen")
            }
        }
    }

    /**
     * 获取插件的 [EventBus].
     */
    fun get(): EventBus = eventBus
}