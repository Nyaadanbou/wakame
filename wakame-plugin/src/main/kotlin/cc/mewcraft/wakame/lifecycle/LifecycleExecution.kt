@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.lifecycle

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.lifecycle.LifecycleExecution.launch
import cc.mewcraft.wakame.lifecycle.LifecycleExecution.launchAll
import com.google.common.graph.Graph
import kotlinx.coroutines.*

/**
 * Internal execution helpers for running lifecycle tasks with dependency graphs.
 */
internal object LifecycleExecution {

    /**
     * Minimal contract a lifecycle task must satisfy to be executed by [launch]/[launchAll].
     */
    internal interface ExecutableTask {
        val completion: Deferred<Unit>
        val dispatcher: CoroutineDispatcher?
        suspend fun execute()
    }

    /**
     * Launches [task] of [graph] in the given [scope].
     *
     * Notes:
     * - Dependencies are re-checked while waiting to support late edge additions.
     * - The task executes in its preferred [ExecutableTask.dispatcher] if present, otherwise inherits [scope]'s context.
     */
    fun <T : Any> launch(
        scope: CoroutineScope,
        task: T,
        graph: Graph<T>,
        toExecutable: (T) -> ExecutableTask,
        logging: Boolean,
    ) {
        scope.launch {
            // Await dependencies. Dependency set might grow during waiting when tasks are registered late.
            var prevDepsSize = -1
            var deps: List<Deferred<*>> = emptyList()

            fun refreshDependencies(): List<Deferred<*>> {
                deps = graph.predecessors(task).map { toExecutable(it).completion }
                return deps
            }

            while (prevDepsSize != refreshDependencies().size) {
                prevDepsSize = deps.size
                deps.awaitAll()
            }

            val executable = toExecutable(task)
            withContext(executable.dispatcher ?: scope.coroutineContext) {
                if (logging) {
                    LOGGER.info(task.toString())
                }
                executable.execute()
            }
        }
    }

    /**
     * Launches all vertices of [graph] in the given [scope].
     */
    fun <T : Any> launchAll(
        scope: CoroutineScope,
        graph: Graph<T>,
        toExecutable: (T) -> ExecutableTask,
        logging: Boolean,
    ) {
        for (node in graph.nodes()) {
            launch(scope, node, graph, toExecutable, logging)
        }
    }
}
