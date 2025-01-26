@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.dependency

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.config.MAIN_CONFIG
import cc.mewcraft.wakame.config.entry
import cc.mewcraft.wakame.initializer2.InitializerRunnable
import cc.mewcraft.wakame.reloader.ReloaderRunnable
import com.google.common.graph.Graph
import kotlinx.coroutines.*

private val LOGGING by MAIN_CONFIG.entry<Boolean>("debug", "logging", "initializer")

@DslMarker
internal annotation class DependencyLoaderSupportDsl

internal fun <T> dependencyLoaderSupport(run: DependencyLoaderSupport.() -> T) =
    DependencyLoaderSupport.run()

@DependencyLoaderSupportDsl
internal object DependencyLoaderSupport {

    internal sealed interface RunnableWrapper<T : Any> {
        val value: T
        fun completion(): Deferred<Unit>
        fun dispatcher(): CoroutineDispatcher?
        suspend fun execute()

        class Init(override val value: InitializerRunnable<*>) : RunnableWrapper<InitializerRunnable<*>> {
            override fun completion(): Deferred<Unit> = value.completion
            override fun dispatcher(): CoroutineDispatcher? = value.dispatcher
            override suspend fun execute() = value.run()
        }

        class Reload(override val value: ReloaderRunnable<*>) : RunnableWrapper<ReloaderRunnable<*>> {
            override fun completion(): Deferred<Unit> = value.completion
            override fun dispatcher(): CoroutineDispatcher? = value.dispatcher
            override suspend fun execute() = value.run()
        }
    }

    fun wrap(runnable: Any): RunnableWrapper<*> {
        return when (runnable) {
            is InitializerRunnable<*> -> RunnableWrapper.Init(runnable)
            is ReloaderRunnable<*> -> RunnableWrapper.Reload(runnable)
            else -> throw IllegalArgumentException("Unsupported runnable type: $runnable")
        }
    }

    /**
     * Launches [runnable] of [graph] in the given [scope].
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : Any> launch(
        scope: CoroutineScope,
        runnable: T,
        graph: Graph<T>,
    ) {
        scope.launch {
            // await dependencies, which may increase during wait
            var prevDepsSize = 0
            var deps: List<Deferred<*>> = emptyList()

            fun findDependencies(): List<Deferred<*>> {
                deps = graph.predecessors(runnable)
                    .map { wrap(it).completion() }
                return deps
            }

            while (prevDepsSize != findDependencies().size) {
                prevDepsSize = deps.size
                deps.awaitAll()
            }

            // run in preferred context
            withContext(wrap(runnable).dispatcher() ?: scope.coroutineContext) {
                if (LOGGING) {
                    LOGGER.info(runnable.toString())
                }

                wrap(runnable).execute()
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