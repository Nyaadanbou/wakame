@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.lifecycle

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.config.MAIN_CONFIG
import cc.mewcraft.wakame.config.optionalEntry
import cc.mewcraft.wakame.lifecycle.initializer.InitializerRunnable
import cc.mewcraft.wakame.lifecycle.reloader.ReloaderRunnable
import com.google.common.graph.Graph
import com.google.common.graph.MutableGraph
import kotlinx.coroutines.*
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.core.LoggerContext
import org.objectweb.asm.Type
import xyz.xenondevs.commons.provider.orElse
import java.lang.reflect.InvocationTargetException
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.callSuspend
import kotlin.reflect.full.functions
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.javaMethod

private val LOGGING by MAIN_CONFIG.optionalEntry<Boolean>("debug", "logging", "initializer").orElse(false)

internal object LifecycleUtils {

    // Executions

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

    private fun wrapTask(task: Any): TaskWrapper<*> {
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
                    .map { wrapTask(it).completion() }
                return deps
            }

            while (prevDepsSize != findDependencies().size) {
                prevDepsSize = deps.size
                deps.awaitAll()
            }

            // run in preferred context
            withContext(wrapTask(task).dispatcher() ?: scope.coroutineContext) {
                if (LOGGING) {
                    LOGGER.info(task.toString())
                }

                wrapTask(task).execute()
            }
        }
    }

    /**
     * Launches all vertices of [graph] in the given [scope].
     */
    fun <T : Any> launchAll(
        scope: CoroutineScope,
        graph: Graph<T>,
    ) {
        for (nodes in graph.nodes()) {
            launch(scope, nodes, graph)
        }
    }

    /**
     * Wraps [run] in a try-catch block with error logging specific to lifecycle.
     * Returns whether the lifecycle has run successful, and also shuts down the server if it wasn't.
     */
    inline fun tryExecute(run: () -> Unit) {
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
    ) {
        val function = clazz.functions.first {
            it.javaMethod!!.name == functionName && it.parameters.size == 1 && it.parameters[0].kind == KParameter.Kind.INSTANCE
        }
        function.isAccessible = true
        function.callSuspend(clazz.objectInstance ?: throw IllegalArgumentException("$clazz.simpleName is not an object"))
        completion.complete(Unit)
    }

    // Graph

    fun <T : Any> tryPutEdge(graph: MutableGraph<T>, from: T, to: T) {
        try {
            graph.putEdge(from, to)
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Failed to add edge from '$from' to '$to'", e)
        }
    }

    // Annotations

    @Suppress("UNCHECKED_CAST")
    inline fun <reified T : Enum<T>> getEnum(name: String, annotationMap: Map<String, Any?>): T? {
        return (annotationMap[name] as Array<String>?)
            ?.get(1)
            ?.let { enumValueOf<T>(it) }
    }

    @Suppress("UNCHECKED_CAST")
    fun getStrings(name: String, annotationMap: Map<String, Any?>): HashSet<String> {
        return (annotationMap[name] as List<Type>?)
            ?.mapTo(HashSet()) { it.internalName }
            ?: HashSet()
    }

    @Suppress("UNCHECKED_CAST")
    fun getDispatcher(annotationMap: Map<String, Any?>): LifecycleDispatcher? {
        return (annotationMap["dispatcher"] as Array<String>?)
            ?.get(1)
            ?.let { enumValueOf<LifecycleDispatcher>(it) }
    }

    fun getAnnotationCommons(annotationMap: Map<String, Any?>): Triple<LifecycleDispatcher?, HashSet<String>, HashSet<String>> {
        val dispatcher = getDispatcher(annotationMap)
        val runBefore = getStrings("runBefore", annotationMap)
        val runAfter = getStrings("runAfter", annotationMap)
        return Triple(dispatcher, runBefore, runAfter)
    }
}
