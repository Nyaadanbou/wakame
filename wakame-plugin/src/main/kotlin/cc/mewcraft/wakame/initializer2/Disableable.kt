@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.initializer2

import cc.mewcraft.wakame.dependency.dependencySupport
import com.google.common.graph.MutableGraph
import kotlinx.coroutines.CoroutineDispatcher

internal class Disableable(
    private val classLoader: ClassLoader,
    private val className: String,
    private val methodName: String,
    override val dispatcher: CoroutineDispatcher?,
    private val runBeforeNames: Set<String>,
    private val runAfterNames: Set<String>
) : InitializerRunnable<Disableable>() {

    override fun loadDependencies(all: Set<Disableable>, graph: MutableGraph<Disableable>) {
        // this runBefore that
        runBeforeNames
            .flatMap { runBeforeName -> all.filter { it.className == runBeforeName } }
            .forEach { graph.putEdge(this, it) }

        // this runAfter that
        runAfterNames
            .flatMap { runAfterName -> all.filter { it.className == runAfterName } }
            .forEach { graph.putEdge(it, this) }
    }

    override suspend fun run() = dependencySupport {
        val clazz = Class.forName(className.replace('/', '.'), true, classLoader).kotlin
        clazz.executeSuspendFunction(methodName, completion)
    }

    override fun toString(): String {
        return "${className}::${methodName}"
    }

    companion object {

        fun fromInitAnnotation(
            classLoader: ClassLoader,
            className: String, methodName: String,
            annotation: Map<String, Any?>
        ) = dependencySupport {
            Disableable(
                classLoader,
                className, methodName,
                (readDispatcher(annotation) ?: Dispatcher.SYNC).dispatcher,
                readStrings("runBefore", annotation),
                readStrings("runAfter", annotation)
            )
        }

    }

}