@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.reloader

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.NEKO
import cc.mewcraft.wakame.event.NekoCommandReloadEvent
import cc.mewcraft.wakame.initializer2.Init
import cc.mewcraft.wakame.initializer2.InitFun
import cc.mewcraft.wakame.initializer2.InitStage
import cc.mewcraft.wakame.initializer2.Initializer
import cc.mewcraft.wakame.initializer2.InitializerSupport.tryInit
import cc.mewcraft.wakame.util.data.JarUtils
import cc.mewcraft.wakame.util.registerSuspendingEvents
import com.google.common.graph.Graph
import com.google.common.graph.GraphBuilder
import com.google.common.graph.MutableGraph
import kotlinx.coroutines.*
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import java.io.File

@Init(
    stage = InitStage.PRE_WORLD
)
internal object Reloader : Listener {

    private val reloadables: HashSet<Reloadable> = HashSet<Reloadable>()

    private val reload: MutableGraph<Reloadable> = GraphBuilder.directed().allowsSelfLoops(false).build()

    /**
     * Stats the reloader process.
     */
    @InitFun
    fun start() = tryInit {
        registerSuspendingEvents()

        collectAndRegisterRunnables(NEKO.nekooJar, this.javaClass.classLoader)
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
            listOf(ReloadableFun::class)
        )

        val internalReloads = result.classes[InternalReload::class] ?: emptyMap()
        val reloads = result.classes[Reload::class] ?: emptyMap()
        val reloadFunctions = result.functions[ReloadableFun::class] ?: emptyMap()

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

        for ((className, annotatedFuncs) in reloadFunctions) {
            val clazz = reloadableClasses[className]
                ?: throw IllegalStateException("Class $className is missing an reload annotation!")

            for ((methodName, annotations) in annotatedFuncs) {
                reloadables += ReloadableFunction.fromInitAnnotation(clazz, methodName, annotations.first())
            }
        }

        return reloadables
    }

    /**
     * Adds the given [Reloadable][Reloadable] and [ReloadableFunction][ReloadableFunction] to the initialization process.
     *
     * This method can only be called before the initialization process has completed.
     */
    private fun addRunnables(reloadables: List<Reloadable>) {
        check(!Initializer.isDone) { "Cannot add runnables after initialization has completed" }

        // add vertices
        for (reloadable in reloadables) {
            this.reloadables += reloadable
            reload.addNode(reloadable)
        }

        // add edges
        for (reloadable in reloadables) {
            reloadable.loadDependencies(
                this.reloadables,
                reload
            )
        }
    }

    /**
     * Reloads all [ReloadableFunctions][ReloadableFunction].
     */
    private fun reload() = runBlocking {
        tryInit {
            coroutineScope {
                launchAll(this, reload)
            }
        }

        LOGGER.info("Done reloading")
    }

    /**
     * Launches all vertices of [graph] in the given [scope].
     */
    private fun <T : ReloaderRunnable<T>> launchAll(scope: CoroutineScope, graph: Graph<T>) {
        for (reloadable in graph.nodes()) {
            launch(scope, reloadable, graph)
        }
    }

    /**
     * Launches [runnable] of [graph] in the given [scope].
     */
    private fun <T : ReloaderRunnable<T>> launch(
        scope: CoroutineScope,
        runnable: T,
        graph: Graph<T>
    ) {
        scope.launch {
            // await dependencies, which may increase during wait
            var prevDepsSize = 0
            var deps: List<Deferred<*>> = emptyList()

            fun findDependencies(): List<Deferred<*>> {
                deps = graph.predecessors(runnable)
                    .map { it.completion }
                return deps
            }

            while (prevDepsSize != findDependencies().size) {
                prevDepsSize = deps.size
                deps.awaitAll()
            }

            // run in preferred context
            withContext(runnable.dispatcher ?: scope.coroutineContext) {
//                if (LOGGING)
                LOGGER.info(runnable.toString())

                runnable.run()
            }
        }
    }

    @EventHandler
    private fun handlePluginReload(event: NekoCommandReloadEvent) {
        if (Initializer.isDone) {
            reload()
        } else LOGGER.warn("Skipping reload because initialization is not done")
    }
}