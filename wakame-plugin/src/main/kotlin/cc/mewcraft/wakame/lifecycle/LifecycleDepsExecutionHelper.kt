@file:Suppress("UnstableApiUsage") @file:JvmName("LifecycleExecutionHelperKt")

package cc.mewcraft.wakame.lifecycle

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.config.MAIN_CONFIG
import cc.mewcraft.wakame.config.entry
import cc.mewcraft.wakame.lifecycle.initializer.InitializerRunnable
import cc.mewcraft.wakame.lifecycle.reloader.ReloaderRunnable
import com.google.common.graph.Graph
import kotlinx.coroutines.*

private val LOGGING by MAIN_CONFIG.entry<Boolean>("debug", "logging", "initializer")

internal fun <T> withLifecycleDependencyExecution(block: LifecycleDependencyExecutionHelper.() -> T): T {
    return block(LifecycleDependencyExecutionHelper)
}

internal object LifecycleDependencyExecutionHelper {

    private sealed interface TaskWrapper<T : Any> {
        val value: T
        fun completion(): Deferred<Unit>
        fun dispatcher(): CoroutineDispatcher?
        suspend fun execute()

        class Init(override val value: InitializerRunnable<*>) : TaskWrapper<InitializerRunnable<*>> {
            override fun completion(): Deferred<Unit> = value.completion
            override fun dispatcher(): CoroutineDispatcher? = value.dispatcher
            override suspend fun execute() = value.run()
        }

        class Reload(override val value: ReloaderRunnable<*>) : TaskWrapper<ReloaderRunnable<*>> {
            override fun completion(): Deferred<Unit> = value.completion
            override fun dispatcher(): CoroutineDispatcher? = value.dispatcher
            override suspend fun execute() = value.run()
        }
    }

    private fun wrap(task: Any): TaskWrapper<*> {
        return when (task) {
            is InitializerRunnable<*> -> TaskWrapper.Init(task)
            is ReloaderRunnable<*> -> TaskWrapper.Reload(task)
            else -> error("Unhandled task type: $task")
        }
    }

    /**
     * Launches [task] of [graph] in the given [scope].
     */
    fun <T : Any> launch(
        scope: CoroutineScope,
        task: T,
        graph: Graph<T>,
    ) {
        scope.launch {
            // await dependencies, which may increase during wait
            var prevDepsSize = 0
            var deps: List<Deferred<*>> = emptyList()

            fun findDependencies(): List<Deferred<*>> {
                deps = graph.predecessors(task)
                    .map { wrap(it).completion() }
                return deps
            }

            while (prevDepsSize != findDependencies().size) {
                prevDepsSize = deps.size
                deps.awaitAll()
            }

            // run in preferred context
            withContext(wrap(task).dispatcher() ?: scope.coroutineContext) {
                if (LOGGING) {
                    LOGGER.info(task.toString())
                }

                wrap(task).execute()
            }
        }
    }

    /**
     * Launches all vertices of [graph] in the given [scope].
     */
    fun <T : Any> launchAll(scope: CoroutineScope, graph: Graph<T>) {
        for (nodes in graph.nodes()) {
            launch(scope, nodes, graph)
        }
    }
}
