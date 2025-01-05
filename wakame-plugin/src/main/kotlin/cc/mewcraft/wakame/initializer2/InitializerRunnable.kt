@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.initializer2

import com.google.common.graph.MutableGraph
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineDispatcher
import org.objectweb.asm.Type

internal sealed class InitializerRunnable<S : InitializerRunnable<S>> {

    val completion = CompletableDeferred<Unit>()
    abstract val dispatcher: CoroutineDispatcher?

    abstract fun loadDependencies(all: Set<S>, graph: MutableGraph<S>)

    abstract suspend fun run()

    companion object {

        @Suppress("UNCHECKED_CAST")
        fun readStrings(name: String, annotation: Map<String, Any?>): HashSet<String> {
            return (annotation[name] as List<Type>?)
                ?.mapTo(HashSet()) { it.internalName }
                ?: HashSet()
        }

        @Suppress("UNCHECKED_CAST")
        fun readDispatcher(annotation: Map<String, Any?>): Dispatcher? {
            return (annotation["dispatcher"] as Array<String>?)
                ?.get(1)
                ?.let { enumValueOf<Dispatcher>(it) }
        }

        fun readAnnotationCommons(annotation: Map<String, Any?>): Triple<Dispatcher?, HashSet<String>, HashSet<String>> {
            val dispatcher = readDispatcher(annotation)
            val runBefore = readStrings("runBefore", annotation)
            val runAfter = readStrings("runAfter", annotation)
            return Triple(dispatcher, runBefore, runAfter)
        }

    }

}