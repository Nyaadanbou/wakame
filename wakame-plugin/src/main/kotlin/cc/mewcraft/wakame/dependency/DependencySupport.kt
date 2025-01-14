@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.dependency

import cc.mewcraft.wakame.initializer2.Dispatcher
import com.google.common.graph.MutableGraph
import kotlinx.coroutines.CompletableDeferred
import org.objectweb.asm.Type
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.callSuspend
import kotlin.reflect.full.functions
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.javaMethod

@DslMarker
internal annotation class DependencySupportDsl

internal inline fun <T> dependencySupport(run: DependencySupport.() -> T) = DependencySupport.run()

@DependencySupportDsl
internal object DependencySupport {
    suspend fun KClass<out Any>.executeSuspendFunction(methodName: String, completion: CompletableDeferred<Unit>) {
        val function = functions.first {
            it.javaMethod!!.name == methodName &&
                    it.parameters.size == 1 &&
                    it.parameters[0].kind == KParameter.Kind.INSTANCE
        }
        function.isAccessible = true
        function.callSuspend(objectInstance)

        completion.complete(Unit)
    }

    fun <T : Any> MutableGraph<T>.tryPutEdge(from: T, to: T) {
        try {
            putEdge(from, to)
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Failed to add edge from '$from' to '$to'", e)
        }
    }

    inline fun <reified T> read(name: String, annotation: Map<String, Any?>): T? {
        return annotation[name] as T?
    }

    @Suppress("UNCHECKED_CAST")
    inline fun <reified T : Enum<T>> readEnum(name: String, annotation: Map<String, Any?>): T? {
        return read<Array<String>>(name, annotation)?.get(1)
            ?.let { enumValueOf<T>(it) }
    }

    @Suppress("UNCHECKED_CAST")
    fun readStrings(name: String, annotation: Map<String, Any?>): HashSet<String> {
        return read<List<Type>>(name, annotation)
            ?.mapTo(HashSet()) { it.internalName }
            ?: HashSet()
    }

    @Suppress("UNCHECKED_CAST")
    fun readDispatcher(annotation: Map<String, Any?>): Dispatcher? {
        return read<Array<String>>("dispatcher", annotation)
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