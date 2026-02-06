@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.lifecycle.initializer

import cc.mewcraft.wakame.lifecycle.LifecycleDispatcher
import cc.mewcraft.wakame.lifecycle.LifecycleUtils
import com.google.common.graph.MutableGraph
import kotlinx.coroutines.CoroutineDispatcher

internal abstract class Initializable(
    val stage: InternalInitStage,
    override val dispatcher: CoroutineDispatcher?,
    private val runBeforeNames: Set<String>,
    private val runAfterNames: Set<String>,
) : InitializerRunnable<Initializable>() {

    abstract val initClass: InitializableClass

    override fun loadDependencies(all: Set<Initializable>, graph: MutableGraph<Initializable>) {
        // this runBefore that
        for (runBeforeName in runBeforeNames) {
            val target = findInitializableClass(all, runBeforeName)
                ?: throw IllegalArgumentException("Could not find initializable class '$runBeforeName', which is a runBefore of '$this'")

            if (!stage.isPreWorld && target.stage.isPreWorld) {
                throw IllegalArgumentException("Incompatible stages: '$this' (post-world) is configured to be initialized before '$runBeforeName' (pre-world)")
            }

            if (target.completion.isCompleted) {
                throw IllegalArgumentException("'$this' is configured to be initialized before '$runBeforeName', but '$runBeforeName' is already initialized")
            }

            // stages are compatible, and execution order is already specified through those
            if (stage != target.stage) {
                continue
            }

            LifecycleUtils.tryPutEdge(graph, this, target)
        }

        // this runAfter that
        for (runAfterName in runAfterNames) {
            val runAfterClass = findInitializableClass(all, runAfterName)
                ?: throw IllegalArgumentException("Could not find initializable class '$runAfterName', which is a runAfter of '$this'")

            val targets = buildSet {
                add(runAfterClass)
                if (runAfterClass != initClass) {
                    addAll(runAfterClass.initFunctions)
                }
            }

            for (target in targets) {
                if (stage.isPreWorld && !target.stage.isPreWorld) {
                    throw IllegalArgumentException("Incompatible stages: '$this' (pre-world) is configured to be initialized after '$runAfterName' (post-world)")
                }

                // stages are compatible, and execution order is already specified through those
                if (stage != target.stage) {
                    continue
                }

                LifecycleUtils.tryPutEdge(graph, target, this)
            }
        }
    }

    companion object {
        fun findInitializableClass(all: Set<Initializable>, className: String): InitializableClass? {
            return all
                .filterIsInstance<InitializableClass>()
                .firstOrNull { candidate -> candidate.initClass.className == className }
        }
    }
}

internal class InitializableClass(
    val classLoader: ClassLoader,
    val className: String,
    stage: InternalInitStage,
    dispatcher: CoroutineDispatcher?,
    runBeforeNames: Set<String>,
    runAfterNames: Set<String>,
) : Initializable(stage, dispatcher, runBeforeNames, runAfterNames) {

    override val initClass = this

    lateinit var clazz: Class<*>
        private set

    val initFunctions = ArrayList<InitializableFunction>()

    override suspend fun run() {
        clazz = Class.forName(className.replace('/', '.'), true, classLoader)
        completion.complete(Unit)
    }

    override fun toString(): String {
        return className
    }

    companion object {

        fun fromAddonAnnotation(classLoader: ClassLoader, clazz: String, annotation: Map<String, Any?>): InitializableClass {
            val stage = LifecycleUtils.getEnum<InitStage>("stage", annotation)
            if (stage == null) throw IllegalStateException("Init annotation on $clazz does not contain a stage!")
            val (dispatcher, runBefore, runAfter) = LifecycleUtils.getAnnotationCommons(annotation)
            runBefore += stage.runBefore
            runAfter += stage.runAfter

            return InitializableClass(
                classLoader = classLoader,
                className = clazz,
                stage = stage.internalStage,
                dispatcher = (dispatcher ?: LifecycleDispatcher.SYNC).dispatcher,
                runBeforeNames = runBefore,
                runAfterNames = runAfter,
            )
        }

        fun fromInternalAnnotation(classLoader: ClassLoader, clazz: String, annotation: Map<String, Any?>): InitializableClass {
            val stage = LifecycleUtils.getEnum<InternalInitStage>("stage", annotation)
            if (stage == null) throw IllegalStateException("InternalInit annotation on $clazz does not contain a stage!")
            val dispatcher = LifecycleUtils.getDispatcher(annotation)
            val dependsOn = LifecycleUtils.getStrings("dependsOn", annotation)

            return InitializableClass(
                classLoader = classLoader,
                className = clazz,
                stage = stage,
                dispatcher = (dispatcher ?: LifecycleDispatcher.SYNC).dispatcher,
                runBeforeNames = emptySet(),
                runAfterNames = dependsOn,
            )
        }
    }
}

internal class InitializableFunction(
    override val initClass: InitializableClass,
    private val methodName: String,
    dispatcher: CoroutineDispatcher?,
    runBeforeNames: Set<String>,
    runAfterNames: Set<String>,
) : Initializable(
    initClass.stage,
    dispatcher,
    runBeforeNames,
    runAfterNames + initClass.className,
) {

    override suspend fun run() {
        val clazz = initClass.clazz.kotlin
        LifecycleUtils.executeSuspendFunction(clazz, methodName, completion)
    }

    override fun toString(): String {
        return "${initClass.className}::$methodName"
    }

    companion object {

        fun fromInitAnnotation(clazz: InitializableClass, methodName: String, annotation: Map<String, Any?>): InitializableFunction {
            val (dispatcher, runBefore, runAfter) = LifecycleUtils.getAnnotationCommons(annotation)
            val func = InitializableFunction(
                initClass = clazz,
                methodName = methodName,
                dispatcher = dispatcher?.dispatcher ?: clazz.dispatcher,
                runBeforeNames = runBefore,
                runAfterNames = runAfter,
            )
            clazz.initFunctions += func
            return func
        }
    }
}