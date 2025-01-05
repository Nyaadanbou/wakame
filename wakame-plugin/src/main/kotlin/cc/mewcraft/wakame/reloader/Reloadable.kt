@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.reloader

import cc.mewcraft.wakame.initializer2.Dispatcher
import cc.mewcraft.wakame.initializer2.InitializerRunnable
import com.google.common.graph.MutableGraph
import kotlinx.coroutines.CoroutineDispatcher
import kotlin.reflect.KParameter
import kotlin.reflect.full.callSuspend
import kotlin.reflect.full.functions
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.javaMethod

internal abstract class Reloadable(
    override val dispatcher: CoroutineDispatcher?,
    private val runBeforeNames: Set<String>,
    private val runAfterNames: Set<String>
) : ReloaderRunnable<Reloadable>() {

    abstract val reloadClass: ReloadableClass

    override fun loadDependencies(all: Set<Reloadable>, graph: MutableGraph<Reloadable>) {
        // this runBefore that
        for (runBeforeName in runBeforeNames) {
            val runBefore = all
                .filterIsInstance<ReloadableClass>()
                .firstOrNull { candidate -> candidate.reloadClass.className == runBeforeName }
                ?: throw IllegalArgumentException("Could not find reloadable class '$runBeforeName', which is a runBefore of '$this'")

            if (runBefore.completion.isCompleted)
                throw IllegalArgumentException("'$this' is configured to be reloaded before '$runBeforeName', but '$runBeforeName' is already reloaded")

            try {
                graph.putEdge(this, runBefore)
            } catch (e: IllegalArgumentException) {
                throw IllegalArgumentException("Failed to add edge from '$this' to '$runBeforeName'", e)
            }
        }

        // this runAfter that
        for (runAfterName in runAfterNames) {
            val runAfters = HashSet<Reloadable>()
            val runAfterClass = all
                .filterIsInstance<ReloadableClass>()
                .firstOrNull { candidate -> candidate.reloadClass.className == runAfterName }
                ?: throw IllegalArgumentException("Could not find reloadable class '$runAfterName', which is a runAfter of '$this'")
            runAfters += runAfterClass
            if (runAfterClass != reloadClass)
                runAfters += runAfterClass.reloadFunctions

            for (runAfter in runAfters) {
                try {
                    graph.putEdge(runAfter, this)
                } catch (e: IllegalArgumentException) {
                    throw IllegalArgumentException("Failed to add edge from '$runAfter' to '$this'", e)
                }
            }
        }
    }

}

internal class ReloadableClass(
    val classLoader: ClassLoader,
    val className: String,
    dispatcher: CoroutineDispatcher?,
    runBeforeNames: Set<String>,
    runAfterNames: Set<String>
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
        fun fromAddonAnnotation(classLoader: ClassLoader, clazz: String, annotation: Map<String, Any?>): ReloadableClass {
            val order = (annotation["order"] as Array<String>?)?.get(1)
                ?.let { enumValueOf<ReloadableOrder>(it) }
                ?: throw IllegalStateException("Reloadable annotation on $clazz does not contain a order!")
            val (dispatcher, runBefore, runAfter) = readAnnotationCommons(annotation)
            runBefore += order.runBefore
            runAfter += order.runAfter

            return ReloadableClass(
                classLoader, clazz,
                (dispatcher ?: Dispatcher.SYNC).dispatcher,
                runBefore, runAfter
            )
        }

        @Suppress("UNCHECKED_CAST")
        fun fromInternalAnnotation(classLoader: ClassLoader, clazz: String, annotation: Map<String, Any?>): ReloadableClass {
            val dispatcher = InitializerRunnable.Companion.readDispatcher(annotation)
            val dependsOn = InitializerRunnable.Companion.readStrings("dependsOn", annotation)

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
    runAfterNames: Set<String>
) : Reloadable(
    dispatcher,
    runBeforeNames,
    runAfterNames + reloadClass.className
) {

    override suspend fun run() {
        val clazz = reloadClass.clazz.kotlin
        val function = clazz.functions.first {
            it.javaMethod!!.name == methodName &&
                    it.parameters.size == 1 &&
                    it.parameters[0].kind == KParameter.Kind.INSTANCE
        }
        function.isAccessible = true
        function.callSuspend(clazz.objectInstance)

        completion.complete(Unit)
    }

    override fun toString(): String {
        return reloadClass.className + "::" + methodName
    }

    companion object {

        fun fromInitAnnotation(clazz: ReloadableClass, methodName: String, annotation: Map<String, Any?>): ReloadableFunction {
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