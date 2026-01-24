@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.lifecycle

import org.objectweb.asm.Type

/**
 * Internal helpers for decoding ASM annotation maps produced by [cc.mewcraft.wakame.util.data.JarUtils].
 */
internal object LifecycleAnnotations {

    @Suppress("UNCHECKED_CAST")
    inline fun <reified T : Enum<T>> getEnum(name: String, annotationMap: Map<String, Any?>): T? {
        return (annotationMap[name] as Array<String>?)
            ?.getOrNull(1)
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
            ?.getOrNull(1)
            ?.let { enumValueOf<LifecycleDispatcher>(it) }
    }

    fun getCommons(annotationMap: Map<String, Any?>): Triple<LifecycleDispatcher?, HashSet<String>, HashSet<String>> {
        val dispatcher = getDispatcher(annotationMap)
        val runBefore = getStrings("runBefore", annotationMap)
        val runAfter = getStrings("runAfter", annotationMap)
        return Triple(dispatcher, runBefore, runAfter)
    }
}
