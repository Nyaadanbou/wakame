package cc.mewcraft.wakame.util.coroutine

import cc.mewcraft.wakame.BootstrapContexts
import cc.mewcraft.wakame.SharedConstants
import cc.mewcraft.wakame.util.runAsyncTask
import cc.mewcraft.wakame.util.runTask
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.bukkit.Bukkit
import kotlin.coroutines.CoroutineContext

/**
 * Minecraft async dispatcher.
 */
val Dispatchers.async: CoroutineDispatcher
    get() = DispatcherContainer.async

/**
 * Minecraft sync dispatcher.
 *
 * 只有当运行环境是真实的 Minecraft 服务端且 Koish 已正确启动时才会实际使用,
 * 否则代码将直接在当前线程执行. 这种行为有点类似 [Dispatchers.Unconfined].
 */
val Dispatchers.minecraft: CoroutineDispatcher
    get() = DispatcherContainer.sync

/**
 * Container for the dispatchers.
 */
private object DispatcherContainer {
    /**
     * Gets the async coroutine context.
     */
    val async: CoroutineDispatcher = AsyncCoroutineDispatcher

    /**
     * Gets the sync coroutine context.
     */
    val sync: CoroutineDispatcher = MinecraftCoroutineDispatcher
}

private object MinecraftCoroutineDispatcher : CoroutineDispatcher() {
    override fun isDispatchNeeded(context: CoroutineContext): Boolean {
        // 以下几种情况不应该使用该 dispatcher:
        // - 代码运行在 IDE 中, 如单元测试
        // - 代码已经运行在服务端的主线程
        // - 插件还未启动完毕
        return !SharedConstants.isRunningInIde && !Bukkit.isPrimaryThread() && BootstrapContexts.PLUGIN_READY
    }

    /**
     * Handles dispatching the coroutine on the correct thread.
     */
    override fun dispatch(context: CoroutineContext, block: Runnable) {
        runTask(block::run)
    }
}

private object AsyncCoroutineDispatcher : CoroutineDispatcher() {
    override fun isDispatchNeeded(context: CoroutineContext): Boolean {
        return true
    }

    /**
     * Handles dispatching the coroutine on the correct thread.
     */
    override fun dispatch(context: CoroutineContext, block: Runnable) {
        runAsyncTask(block::run)
    }
}
