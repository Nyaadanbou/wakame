package cc.mewcraft.wakame.lifecycle.reloader

import cc.mewcraft.wakame.KOISH_JAR
import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.event.NekoCommandReloadEvent
import cc.mewcraft.wakame.lifecycle.LifecycleExecutionHelper.tryExecute
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.lifecycle.initializer.Initializer
import cc.mewcraft.wakame.lifecycle.withLifecycleDependencyExecution
import cc.mewcraft.wakame.util.data.JarUtils
import cc.mewcraft.wakame.util.registerEvents
import com.google.common.graph.GraphBuilder
import com.google.common.graph.MutableGraph
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import java.io.File

@Init(
    stage = InitStage.PRE_WORLD
)
internal object Reloader : Listener {

    private val reloadables: HashSet<Reloadable> = HashSet()
    private val dependencyGraph: MutableGraph<Reloadable> = GraphBuilder.directed().allowsSelfLoops(false).build()

    /**
     * Stats the reloading process.
     */
    @InitFun
    private fun start() = tryExecute {
        registerEvents()
        collectAndRegisterRunnables(KOISH_JAR.toFile(), this.javaClass.classLoader)
    }

    private fun collectAndRegisterRunnables(file: File, classLoader: ClassLoader) {
        val reloadables = collectRunnables(file, classLoader)
        addRunnables(reloadables)
    }

    private fun collectRunnables(file: File, classLoader: ClassLoader): List<Reloadable> {
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
    private fun addRunnables(reloadables: List<Reloadable>) {
        check(!Initializer.isDone) { "Cannot add runnables after initialization has completed" }

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
    private fun reload() = withLifecycleDependencyExecution {
        runBlocking {
            tryExecute {
                coroutineScope {
                    launchAll(this, dependencyGraph)
                }
            }

            LOGGER.info("Done reloading")
        }
    }

    @EventHandler
    private fun handlePluginReload(event: NekoCommandReloadEvent) {
        if (Initializer.isDone) {
            reload()
        } else {
            LOGGER.error("Skipping reload because initialization is not done")
        }
    }
}