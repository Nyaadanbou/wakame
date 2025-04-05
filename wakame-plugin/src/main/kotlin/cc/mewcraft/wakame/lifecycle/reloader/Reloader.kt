package cc.mewcraft.wakame.lifecycle.reloader

import cc.mewcraft.wakame.BootstrapContextStore
import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.event.map.ConfigurationReloadEvent
import cc.mewcraft.wakame.lifecycle.helper.TryExecution.tryExecute
import cc.mewcraft.wakame.lifecycle.helper.withLifecycleDependencyExecution
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.Initializer
import cc.mewcraft.wakame.lifecycle.initializer.InternalInit
import cc.mewcraft.wakame.lifecycle.initializer.InternalInitStage
import cc.mewcraft.wakame.util.data.JarUtils
import cc.mewcraft.wakame.util.eventbus.MapEventBus
import com.google.common.graph.GraphBuilder
import com.google.common.graph.MutableGraph
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import java.io.File

@InternalInit(
    stage = InternalInitStage.PRE_WORLD,
)
internal object Reloader {

    private val reloadables: HashSet<Reloadable> = HashSet()
    private val dependencyGraph: MutableGraph<Reloadable> = GraphBuilder.directed().allowsSelfLoops(false).build()

    @InitFun
    fun collectAndRegisterTasks() = tryExecute {
        registerTasks(collectTasks(BootstrapContextStore.PLUGIN_JAR.toFile(), this.javaClass.classLoader))
    }

    private fun collectTasks(file: File, classLoader: ClassLoader): List<Reloadable> {
        val reloadables = ArrayList<Reloadable>()
        val reloadableClasses = HashMap<String, ReloadableClass>()

        val result = JarUtils.findAnnotatedClasses(
            file,
            listOf(InternalReload::class, Reload::class),
            listOf(ReloadFun::class)
        )

        val internalReloads = result.classes[InternalReload::class] ?: emptyMap()
        val reloads = result.classes[Reload::class] ?: emptyMap()
        val reloadFunctions = result.functions[ReloadFun::class] ?: emptyMap()

        for ((className, annotations) in internalReloads) {
            val clazz = ReloadableClass.fromInternalAnnotation(classLoader, className, annotations.first())
            reloadables += clazz
            reloadableClasses[className] = clazz
        }

        for ((className, annotations) in reloads) {
            val clazz = ReloadableClass.fromAddonAnnotation(classLoader, className, annotations.first())
            reloadables += clazz
            reloadableClasses[className] = clazz
        }

        for ((className, annotatedFunctions) in reloadFunctions) {
            val clazz = reloadableClasses[className]
                ?: throw IllegalStateException("Class $className is missing an reload annotation!")

            for ((methodName, annotations) in annotatedFunctions) {
                reloadables += ReloadableFunction.fromInitAnnotation(clazz, methodName, annotations.first())
            }
        }

        return reloadables
    }

    /**
     * Adds the given [Reloadable][Reloadable] and [ReloadableFunction][ReloadableFunction] to the reloading process.
     *
     * This method can only be called before the reloading process has completed.
     */
    private fun registerTasks(reloadables: List<Reloadable>) {
        check(!Initializer.isDone) { "Cannot register tasks after initialization has completed" }

        // add vertices
        for (reloadable in reloadables) {
            Reloader.reloadables += reloadable
            dependencyGraph.addNode(reloadable)
        }

        // add edges
        for (reloadable in reloadables) {
            reloadable.loadDependencies(
                Reloader.reloadables,
                dependencyGraph
            )
        }
    }

    /**
     * Reloads all [ReloadableFunctions][ReloadableFunction].
     */
    internal fun reload() = withLifecycleDependencyExecution {
        if (Initializer.isDone) {
            runBlocking {
                LOGGER.info("Calling Reload Functions")
                tryExecute {
                    coroutineScope {
                        launchAll(this, dependencyGraph)
                    }
                }

                LOGGER.info("Calling Reload Events")
                MapEventBus.post(ConfigurationReloadEvent)

                LOGGER.info("Done reloading")
            }
        } else {
            LOGGER.error("Skipping reload because initialization is not done")
        }
    }

}