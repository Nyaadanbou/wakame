@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.lifecycle

import com.google.common.graph.MutableGraph
import kotlinx.coroutines.CompletableDeferred
import org.objectweb.asm.Type
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.callSuspend
import kotlin.reflect.full.functions
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.javaMethod

internal inline fun <T> withLifecycleDependencyCreation(block: LifecycleDependencyCreationHelper.() -> T): T {
    return block(LifecycleDependencyCreationHelper)
}

internal object LifecycleDependencyCreationHelper {
    suspend fun KClass<out Any>.executeSuspendFunction(
        functionName: String,
        completion: CompletableDeferred<Unit>
    ) {
        val function = functions.first {
            it.javaMethod!!.name == functionName &&
                    it.parameters.size == 1 &&
                    it.parameters[0].kind == KParameter.Kind.INSTANCE
        }
        function.isAccessible = true
        function.callSuspend(objectInstance ?: throw IllegalArgumentException("$simpleName is not an object"))
        completion.complete(Unit)
    }

    fun <T : Any> MutableGraph<T>.tryPutEdge(from: T, to: T) {
        try {
            putEdge(from, to)
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Failed to add edge from '$from' to '$to'", e)
        }
    }

    inline fun <reified T> get(name: String, annotationMap: Map<String, Any?>): T? {
        return annotationMap[name] as T?
    }

    inline fun <reified T : Enum<T>> getEnum(name: String, annotationMap: Map<String, Any?>): T? {
        return get<Array<String>>(name, annotationMap)?.get(1)
            ?.let { enumValueOf<T>(it) }
    }

    fun getStrings(name: String, annotationMap: Map<String, Any?>): HashSet<String> {
        return get<List<Type>>(name, annotationMap)
            ?.mapTo(HashSet()) { it.internalName }
            ?: HashSet()
    }

    fun getDispatcher(annotationMap: Map<String, Any?>): LifecycleDispatcher? {
        return get<Array<String>>("dispatcher", annotationMap)
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
