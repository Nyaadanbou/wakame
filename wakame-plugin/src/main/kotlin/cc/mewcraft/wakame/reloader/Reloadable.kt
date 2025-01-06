@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.reloader

import cc.mewcraft.wakame.config.Configs
import cc.mewcraft.wakame.dependency.dependencySupport
import cc.mewcraft.wakame.initializer2.Dispatcher
import cc.mewcraft.wakame.util.internalName
import com.google.common.graph.MutableGraph
import kotlinx.coroutines.CoroutineDispatcher

internal abstract class Reloadable(
    override val dispatcher: CoroutineDispatcher?,
    private val runBeforeNames: Set<String>,
    private val runAfterNames: Set<String>,
) : ReloaderRunnable<Reloadable>() {

    abstract val reloadClass: ReloadableClass

    override fun loadDependencies(all: Set<Reloadable>, graph: MutableGraph<Reloadable>) = dependencySupport {
        // this runBefore that
        for (runBeforeName in runBeforeNames) {
            val runBefore = findReloadClass(all, runBeforeName)
                ?: throw IllegalArgumentException("Could not find reloadable class '$runBeforeName', which is a runBefore of '$this'")

            if (runBefore.completion.isCompleted)
                throw IllegalArgumentException("'$this' is configured to be reloaded before '$runBeforeName', but '$runBeforeName' is already reloaded")

            graph.tryPutEdge(this@Reloadable, runBefore)
        }

        // this runAfter that
        for (runAfterName in runAfterNames) {
            val runAfters = HashSet<Reloadable>()
            val runAfterClass = findReloadClass(all, runAfterName)
                ?: throw IllegalArgumentException("Could not find reloadable class '$runAfterName', which is a runAfter of '$this'")
            runAfters += runAfterClass

            if (runAfterClass != reloadClass)
                runAfters += runAfterClass.reloadFunctions

            for (runAfter in runAfters) {
                graph.tryPutEdge(runAfter, this@Reloadable)
            }
        }
    }

    companion object {
        fun findReloadClass(all: Set<Reloadable>, className: String): ReloadableClass? {
            return all.filterIsInstance<ReloadableClass>()
                .firstOrNull { candidate -> candidate.reloadClass.className == className }
        }
    }
}

internal class ReloadableClass(
    val classLoader: ClassLoader,
    val className: String,
    dispatcher: CoroutineDispatcher?,
    runBeforeNames: Set<String>,
    runAfterNames: Set<String>,
) : Reloadable(dispatcher, runBeforeNames, runAfterNames) {

    override val reloadClass = this

    lateinit var clazz: Class<*>
        private set

    val reloadFunctions = ArrayList<ReloadableFunction>()

    override suspend fun run() {
        clazz = Class.forName(className.replace('/', '.'), true, classLoader)
        completion.complete(Unit)
    }

    override fun toString(): String {
        return className
    }

    companion object {

        @Suppress("UNCHECKED_CAST")
        fun fromAddonAnnotation(classLoader: ClassLoader, clazz: String, annotation: Map<String, Any?>): ReloadableClass = dependencySupport{
            val (dispatcher, runBefore, runAfter) = readAnnotationCommons(annotation)
            // Configs 需要在所有类之前重载之前被重载.
            runBefore += Configs::class.internalName

            return ReloadableClass(
                classLoader, clazz,
                (dispatcher ?: Dispatcher.SYNC).dispatcher,
                runBefore, runAfter
            )
        }

        @Suppress("UNCHECKED_CAST")
        fun fromInternalAnnotation(classLoader: ClassLoader, clazz: String, annotation: Map<String, Any?>): ReloadableClass = dependencySupport{
            val dispatcher = readDispatcher(annotation)
            val dependsOn = readStrings("dependsOn", annotation)

            return ReloadableClass(
                classLoader, clazz,
                (dispatcher ?: Dispatcher.SYNC).dispatcher,
                emptySet(), dependsOn
            )
        }

    }

}

internal class ReloadableFunction(
    override val reloadClass: ReloadableClass,
    private val methodName: String,
    dispatcher: CoroutineDispatcher?,
    runBeforeNames: Set<String>,
    runAfterNames: Set<String>,
) : Reloadable(
    dispatcher,
    runBeforeNames,
    runAfterNames + reloadClass.className
) {

    override suspend fun run() = dependencySupport {
        val clazz = reloadClass.clazz.kotlin
        clazz.executeSuspendFunction(methodName, completion)
    }

    override fun toString(): String {
        return reloadClass.className + "::" + methodName
    }

    companion object {

        fun fromInitAnnotation(clazz: ReloadableClass, methodName: String, annotation: Map<String, Any?>): ReloadableFunction = dependencySupport {
            val (dispatcher, runBefore, runAfter) = readAnnotationCommons(annotation)
            val func = ReloadableFunction(
                clazz, methodName,
                dispatcher?.dispatcher ?: clazz.dispatcher,
                runBefore, runAfter
            )
            clazz.reloadFunctions += func
            return func
        }

    }

}