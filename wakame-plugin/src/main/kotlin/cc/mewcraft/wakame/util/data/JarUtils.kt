package cc.mewcraft.wakame.util.data

import org.objectweb.asm.ClassReader
import org.objectweb.asm.Type
import org.objectweb.asm.tree.AnnotationNode
import org.objectweb.asm.tree.ClassNode
import java.util.jar.JarInputStream
import kotlin.reflect.KClass
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.URI
import java.net.URISyntaxException
import java.net.URL
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.util.jar.JarEntry
import java.util.jar.JarFile

internal data class AnnotationSearchResult(
    val classes: Map<KClass<out Annotation>, Map<String, List<Map<String, Any?>>>>,
    val functions: Map<KClass<out Annotation>, Map<String, Map<String, List<Map<String, Any?>>>>>,
)

internal object JarUtils {

    fun extractJar(classLoader: ClassLoader, targetDirectory: String, destinationDirectory: File) {
        try {
            if (!destinationDirectory.exists()) {
                destinationDirectory.mkdirs()
            }

            var adjustedTargetDirectory = targetDirectory
            if (!adjustedTargetDirectory.endsWith("/")) {
                adjustedTargetDirectory += "/"
            }

            val resourceUrl: URL = classLoader.getResource(adjustedTargetDirectory)
                ?: throw FileNotFoundException("Resource not found: $adjustedTargetDirectory")

            val jarPath = try {
                val uri: URI = resourceUrl.toURI()
                val decodedUri = URLDecoder.decode(uri.toString(), StandardCharsets.UTF_8.name())
                when {
                    decodedUri.startsWith("jar:file:") -> decodedUri.substring(9, decodedUri.indexOf("!"))
                    decodedUri.startsWith("file:") -> decodedUri.substring(5)
                    else -> throw IOException("Unexpected URI scheme in $decodedUri")
                }
            } catch (e: URISyntaxException) {
                throw IOException("Invalid URI syntax: $resourceUrl", e)
            }

            JarFile(File(jarPath)).use { jar ->
                val entries = jar.entries()
                while (entries.hasMoreElements()) {
                    val entry: JarEntry = entries.nextElement()
                    val entryName = entry.name

                    if (entryName.startsWith(adjustedTargetDirectory)) {
                        var relativePath = entryName.substring(adjustedTargetDirectory.length)

                        if (relativePath.startsWith("/")) {
                            relativePath = relativePath.substring(1)
                        }

                        val entryDestination = File(destinationDirectory, relativePath)

                        if (entry.isDirectory) {
                            entryDestination.mkdirs()
                        } else {
                            entryDestination.parentFile?.takeIf { !it.exists() }?.mkdirs()

                            jar.getInputStream(entry).use { input ->
                                FileOutputStream(entryDestination).use { output ->
                                    copy(input, output)
                                }
                            }
                        }
                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun copy(input: InputStream, output: OutputStream) {
        val buffer = ByteArray(1024)
        var len: Int
        while (input.read(buffer).also { len = it } != -1) {
            output.write(buffer, 0, len)
        }
    }

    // TODO: find annotated classes during build and write them to a file 
    fun findAnnotatedClasses(
        file: File,
        classAnnotations: List<KClass<out Annotation>>,
        functionAnnotations: List<KClass<out Annotation>>,
        path: String = "",
    ): AnnotationSearchResult {
        val classes = HashMap<KClass<out Annotation>, MutableMap<String, MutableList<Map<String, Any?>>>>()
        val functions = HashMap<KClass<out Annotation>, MutableMap<String, MutableMap<String, MutableList<Map<String, Any?>>>>>()
        val classAnnotationDescriptors = classAnnotations.map { Type.getDescriptor(it.java) }
        val functionAnnotationDescriptors = functionAnnotations.map { Type.getDescriptor(it.java) }

        loopClasses(file, filter = { it.name.endsWith(".class") && it.name.startsWith(path) }) { _, ins ->
            val classNode = ClassNode().apply { ClassReader(ins).accept(this, ClassReader.SKIP_CODE) }

            classNode.visibleAnnotations?.forEach { annotation ->
                val i = classAnnotationDescriptors.indexOf(annotation.desc)
                if (i == -1)
                    return@forEach

                classes
                    .getOrPut(classAnnotations[i], ::HashMap)
                    .getOrPut(classNode.name, ::ArrayList)
                    .add(annotation.toMap())
            }

            for (method in classNode.methods) {
                method.visibleAnnotations?.forEach { annotation ->
                    val i = functionAnnotationDescriptors.indexOf(annotation.desc)
                    if (i == -1)
                        return@forEach

                    functions
                        .getOrPut(functionAnnotations[i], ::HashMap)
                        .getOrPut(classNode.name, ::HashMap)
                        .getOrPut(method.name, ::ArrayList)
                        .add(annotation.toMap())
                }
            }
        }

        return AnnotationSearchResult(classes, functions)
    }

    private fun loopClasses(file: File, filter: (JarEntry) -> Boolean = { true }, action: (JarEntry, InputStream) -> Unit) {
        JarInputStream(file.inputStream()).use { jis ->
            generateSequence(jis::getNextJarEntry)
                .filter(filter)
                .forEach { entry -> action(entry, jis) }
        }
    }

    /**
     * Converts an [AnnotationNode] to a map ``name -> value``
     */
    private fun AnnotationNode.toMap(): Map<String, Any?> {
        if (values.isNullOrEmpty())
            return emptyMap()

        val map = mutableMapOf<String, Any?>()
        for (i in 0 until this.values.size step 2) {
            map[this.values[i].toString()] = this.values[i + 1]
        }
        return map
    }

}