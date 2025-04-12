@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.lifecycle.reloader

import cc.mewcraft.wakame.config.Configs
import cc.mewcraft.wakame.lifecycle.LifecycleDispatcher
import cc.mewcraft.wakame.lifecycle.LifecycleUtils
import cc.mewcraft.wakame.util.internalName
import com.google.common.graph.MutableGraph
import kotlinx.coroutines.CoroutineDispatcher

internal abstract class Reloadable(
    override val dispatcher: CoroutineDispatcher?,
    private val runBeforeNames: Set<String>,
    private val runAfterNames: Set<String>,
) : ReloaderRunnable<Reloadable>() {

    abstract val reloadClass: ReloadableClass

    override fun loadDependencies(all: Set<Reloadable>, graph: MutableGraph<Reloadable>) {
        // this runBefore that
        for (runBeforeName in runBeforeNames) {
            val runBefore = findReloadClass(all, runBeforeName)
            if (runBefore == null) throw IllegalArgumentException("Could not find reloadable class '$runBeforeName', which is a runBefore of '${this}'")

            if (runBefore.completion.isCompleted) {
                throw IllegalArgumentException("'${this}' is configured to be reloaded before '$runBeforeName', but '$runBeforeName' is already reloaded")
            }

            LifecycleUtils.tryPutEdge(graph, this, runBefore)
        }

        // this runAfter that
        for (runAfterName in runAfterNames) {
            val runAfters = HashSet<Reloadable>()
            val runAfterClass = findReloadClass(all, runAfterName)
            if (runAfterClass == null) throw IllegalArgumentException("Could not find reloadable class '$runAfterName', which is a runAfter of '${this}'")
            runAfters += runAfterClass

            if (runAfterClass != reloadClass) {
                runAfters += runAfterClass.reloadFunctions
            }

            for (runAfter in runAfters) {
                LifecycleUtils.tryPutEdge(graph, runAfter, this)
            }
        }
    }

    companion object {
        fun findReloadClass(all: Set<Reloadable>, className: String): ReloadableClass? {
            return all
                .filterIsInstance<ReloadableClass>()
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

        fun fromAddonAnnotation(classLoader: ClassLoader, clazz: String, annotation: Map<String, Any?>): ReloadableClass {
            val (dispatcher, runBefore, runAfter) = LifecycleUtils.getAnnotationCommons(annotation)
            // Configs 需要在所有类之前重载之前被重载.
            runBefore += Configs::class.internalName

            return ReloadableClass(
                classLoader, clazz, (dispatcher ?: LifecycleDispatcher.SYNC).dispatcher, runBefore, runAfter
            )
        }

        fun fromInternalAnnotation(classLoader: ClassLoader, clazz: String, annotation: Map<String, Any?>): ReloadableClass {
            val dispatcher = LifecycleUtils.getDispatcher(annotation)
            val dependsOn = LifecycleUtils.getStrings("dependsOn", annotation)

            return ReloadableClass(
                classLoader,
                clazz,
                (dispatcher ?: LifecycleDispatcher.SYNC).dispatcher,
                emptySet(),
                dependsOn
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

    override suspend fun run() {
        val clazz = reloadClass.clazz.kotlin
        LifecycleUtils.executeSuspendFunction(clazz, methodName, completion)
    }

    override fun toString(): String {
        return reloadClass.className + "::" + methodName
    }

    companion object {

        fun fromInitAnnotation(clazz: ReloadableClass, methodName: String, annotation: Map<String, Any?>): ReloadableFunction {
            val (dispatcher, runBefore, runAfter) = LifecycleUtils.getAnnotationCommons(annotation)
            val func = ReloadableFunction(
                clazz,
                methodName,
                dispatcher?.dispatcher ?: clazz.dispatcher,
                runBefore,
                runAfter
            )
            clazz.reloadFunctions += func
            return func
        }
    }
}