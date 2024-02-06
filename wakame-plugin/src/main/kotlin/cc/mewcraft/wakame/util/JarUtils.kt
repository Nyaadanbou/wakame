package cc.mewcraft.wakame.util

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import org.objectweb.asm.ClassReader
import org.objectweb.asm.Type
import org.objectweb.asm.tree.AnnotationNode
import org.objectweb.asm.tree.ClassNode
import java.io.File
import java.io.InputStream
import java.util.jar.JarEntry
import java.util.jar.JarInputStream
import kotlin.reflect.KClass

/**
 * Converts an [AnnotationNode] to a map `name -> value`
 */
fun AnnotationNode.toMap(): Map<String, Any?> {
    if (values.isNullOrEmpty())
        return emptyMap()

    val map = mutableMapOf<String, Any?>()
    for (i in 0 until this.values.size step 2) {
        map[this.values[i].toString()] = this.values[i + 1]
    }
    return map
}

object JarUtils {

    // TODO: find annotated classes during build and write them to a file

    fun <A : Annotation> findAnnotatedClasses(file: File, annotationClass: KClass<A>, path: String = ""): Map<String, Map<String, Any?>> {
        val result = Object2ObjectOpenHashMap<String, Map<String, Any?>>()
        val classDesc = Type.getDescriptor(annotationClass.java)

        loopClasses(file, filter = { it.name.endsWith(".class") && it.name.startsWith(path) }) { _, ins ->
            val clazz = ClassNode().apply { ClassReader(ins).accept(this, ClassReader.SKIP_CODE) }
            val annotation = clazz.visibleAnnotations?.firstOrNull { it.desc == classDesc } ?: return@loopClasses
            result[clazz.name] = annotation.toMap()
        }

        return result
    }

    fun loopClasses(file: File, filter: (JarEntry) -> Boolean = { true }, action: (JarEntry, InputStream) -> Unit) {
        JarInputStream(file.inputStream()).use { jis ->
            generateSequence(jis::getNextJarEntry).filter(filter).forEach { entry ->
                action(entry, jis)
            }
        }
    }

}