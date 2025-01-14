@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.initializer2

import cc.mewcraft.wakame.dependency.dependencySupport
import com.google.common.graph.MutableGraph
import kotlinx.coroutines.CoroutineDispatcher

internal abstract class Initializable(
    val stage: InternalInitStage,
    override val dispatcher: CoroutineDispatcher?,
    private val runBeforeNames: Set<String>,
    private val runAfterNames: Set<String>,
) : InitializerRunnable<Initializable>() {

    abstract val initClass: InitializableClass

    override fun loadDependencies(all: Set<Initializable>, graph: MutableGraph<Initializable>) = dependencySupport {
        // this runBefore that
        for (runBeforeName in runBeforeNames) {
            val runBefore = findInitializableClass(all, runBeforeName)
                ?: throw IllegalArgumentException("Could not find initializable class '$runBeforeName', which is a runBefore of '${this@Initializable}'")
            if (!stage.isPreWorld && runBefore.stage.isPreWorld)
                throw IllegalArgumentException("Incompatible stages: '${this@Initializable}' (post-world) is configured to be initialized before '$runBeforeName' (pre-world)")

            if (runBefore.completion.isCompleted)
                throw IllegalArgumentException("'${this@Initializable}' is configured to be initialized before '$runBeforeName', but '$runBeforeName' is already initialized")

            // stages are compatible, and execution order is already specified through those
            if (stage != runBefore.stage)
                continue

            graph.tryPutEdge(this@Initializable, runBefore)
        }

        // this runAfter that
        for (runAfterName in runAfterNames) {
            val runAfters = HashSet<Initializable>()
            val runAfterClass = findInitializableClass(all, runAfterName)
                ?: throw IllegalArgumentException("Could not find initializable class '$runAfterName', which is a runAfter of '${this@Initializable}'")
            runAfters += runAfterClass
            if (runAfterClass != initClass)
                runAfters += runAfterClass.initFunctions

            for (runAfter in runAfters) {
                if (stage.isPreWorld && !runAfter.stage.isPreWorld)
                    throw IllegalArgumentException("Incompatible stages: '${this@Initializable}' (pre-world) is configured to be initialized after '$runAfterName' (post-world)")

                // stages are compatible, and execution order is already specified through those
                if (stage != runAfter.stage)
                    continue

                graph.tryPutEdge(runAfter, this@Initializable)
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

        @Suppress("UNCHECKED_CAST")
        fun fromAddonAnnotation(classLoader: ClassLoader, clazz: String, annotation: Map<String, Any?>): InitializableClass = dependencySupport {
            val stage = readEnum<InitStage>("stage", annotation)
                ?: throw IllegalStateException("Init annotation on $clazz does not contain a stage!")
            val (dispatcher, runBefore, runAfter) = readAnnotationCommons(annotation)
            runBefore += stage.runBefore
            runAfter += stage.runAfter

            return InitializableClass(
                classLoader, clazz,
                stage.internalStage, (dispatcher ?: Dispatcher.SYNC).dispatcher,
                runBefore, runAfter
            )
        }

        @Suppress("UNCHECKED_CAST")
        fun fromInternalAnnotation(classLoader: ClassLoader, clazz: String, annotation: Map<String, Any?>): InitializableClass = dependencySupport {
            val stage = readEnum<InternalInitStage>("stage", annotation)
                ?: throw IllegalStateException("InternalInit annotation on $clazz does not contain a stage!")
            val dispatcher = readDispatcher(annotation)
            val dependsOn = readStrings("dependsOn", annotation)

            return InitializableClass(
                classLoader, clazz,
                stage, (dispatcher ?: Dispatcher.SYNC).dispatcher,
                emptySet(), dependsOn
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
    runAfterNames + initClass.className
) {

    override suspend fun run() = dependencySupport {
        val clazz = initClass.clazz.kotlin
        clazz.executeSuspendFunction(methodName, completion)
    }

    override fun toString(): String {
        return initClass.className + "::" + methodName
    }

    companion object {

        fun fromInitAnnotation(clazz: InitializableClass, methodName: String, annotation: Map<String, Any?>): InitializableFunction = dependencySupport {
            val (dispatcher, runBefore, runAfter) = readAnnotationCommons(annotation)
            val func = InitializableFunction(
                clazz, methodName,
                dispatcher?.dispatcher ?: clazz.dispatcher,
                runBefore, runAfter
            )
            clazz.initFunctions += func
            return func
        }

    }

}