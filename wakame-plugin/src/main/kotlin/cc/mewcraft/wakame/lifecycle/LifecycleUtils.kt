@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.lifecycle

import cc.mewcraft.lazyconfig.MAIN_CONFIG
import cc.mewcraft.lazyconfig.access.optionalEntry
import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.lifecycle.initializer.InitializerRunnable
import com.google.common.graph.Graph
import com.google.common.graph.MutableGraph
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.core.LoggerContext
import xyz.xenondevs.commons.provider.orElse
import java.lang.reflect.InvocationTargetException
import kotlin.reflect.KClass

private val LOGGING by MAIN_CONFIG.optionalEntry<Boolean>("debug", "logging", "initializer").orElse(false)

internal object LifecycleUtils {

    // Executions

    private fun asExecutable(task: InitializerRunnable<*>): LifecycleExecution.ExecutableTask {
        return object : LifecycleExecution.ExecutableTask {
            override val completion: Deferred<Unit> get() = task.completion
            override val dispatcher: CoroutineDispatcher? get() = task.dispatcher
            override suspend fun execute() = task.run()
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
        LifecycleExecution.launch(
            scope = scope,
            task = task,
            graph = graph,
            toExecutable = { node ->
                when (node) {
                    is InitializerRunnable<*> -> asExecutable(node)
                    else -> error("Unhandled task type: $node")
                }
            },
            logging = LOGGING,
        )
    }

    /**
     * Launches all vertices of [graph] in the given [scope].
     */
    fun <T : Any> launchAll(
        scope: CoroutineScope,
        graph: Graph<T>,
    ) {
        LifecycleExecution.launchAll(
            scope = scope,
            graph = graph,
            toExecutable = { node ->
                when (node) {
                    is InitializerRunnable<*> -> asExecutable(node)
                    else -> error("Unhandled task type: $node")
                }
            },
            logging = LOGGING,
        )
    }

    /**
     * Wraps [run] in a try-catch block with error logging specific to lifecycle.
     */
    inline fun runLifecycle(run: () -> Unit) {
        try {
            run()
        } catch (t: Throwable) {
            val cause = if (t is InvocationTargetException) t.targetException else t
            if (cause is LifecycleException) {
                LOGGER.error(cause.message)
            } else {
                LOGGER.error("An exception occurred during lifecycle execution", cause)
            }

            LOGGER.error("Lifecycle task failure")
            (LogManager.getContext(false) as LoggerContext).stop() // flush log messages
            Runtime.getRuntime().halt(-1) // force-quit process to prevent further errors
        }
    }

    // Reflections

    suspend fun executeSuspendFunction(
        clazz: KClass<out Any>,
        functionName: String,
        completion: CompletableDeferred<Unit>,
    ) = LifecycleReflection.executeSuspendFunction(clazz, functionName, completion)

    // Graph

    fun <T : Any> tryPutEdge(graph: MutableGraph<T>, from: T, to: T) = LifecycleGraph.tryPutEdge(graph, from, to)

    // Annotations

    inline fun <reified T : Enum<T>> getEnum(name: String, annotationMap: Map<String, Any?>): T? =
        LifecycleAnnotations.getEnum(name, annotationMap)

    fun getStrings(name: String, annotationMap: Map<String, Any?>): HashSet<String> =
        LifecycleAnnotations.getStrings(name, annotationMap)

    fun getDispatcher(annotationMap: Map<String, Any?>): LifecycleDispatcher? =
        LifecycleAnnotations.getDispatcher(annotationMap)

    fun getAnnotationCommons(annotationMap: Map<String, Any?>): Triple<LifecycleDispatcher?, HashSet<String>, HashSet<String>> =
        LifecycleAnnotations.getCommons(annotationMap)
}
