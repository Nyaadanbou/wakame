@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.initializer

import cc.mewcraft.wakame.NEKO_PLUGIN
import cc.mewcraft.wakame.WakamePlugin
import cc.mewcraft.wakame.event.NekoLoadDataEvent
import cc.mewcraft.wakame.registry.*
import cc.mewcraft.wakame.test.TestListener
import cc.mewcraft.wakame.util.callEvent
import cc.mewcraft.wakame.util.registerEvents
import cc.mewcraft.wakame.util.unregisterEvents
import com.github.shynixn.mccoroutine.bukkit.launch
import com.google.common.graph.Graph
import com.google.common.graph.GraphBuilder
import com.google.common.graph.MutableGraph
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.joinAll
import me.lucko.helper.terminable.composite.CompositeTerminable
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.logger.slf4j.ComponentLogger
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerLoginEvent
import org.bukkit.event.server.ServerLoadEvent
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation

/**
 * @see Initializable
 */
object Initializer : KoinComponent, Listener {

    private val logger: ComponentLogger by inject(mode = LazyThreadSafetyMode.NONE)
    private val plugin: WakamePlugin by inject(mode = LazyThreadSafetyMode.NONE)
    private val terminables: CompositeTerminable = CompositeTerminable.create()

    private val toInitPreWorld: MutableList<Initializable> = ArrayList()
    private val toInitPostWorld: MutableList<Initializable> = ArrayList()

    /**
     * Whether the pre-world initialization has been completed successfully.
     */
    private var preWorldInitialized = false

    var isDone: Boolean = false
        private set

    /**
     * Should be called before the world is loaded.
     */
    fun start() {
        handleDependencies()
        initPreWorld()
    }

    private fun initConfig() = with(plugin) {
        saveDefaultConfig() // config.yml
        saveResourceRecursively(CRATE_CONFIG_DIR)
        saveResourceRecursively(ITEM_CONFIG_DIR)
        saveResourceRecursively(SKILL_CONFIG_DIR)
        saveResource(ATTRIBUTE_CONFIG_FILE)
        saveResource(ELEMENT_CONFIG_FILE)
        saveResource(CATEGORY_CONFIG_FILE)
        saveResource(KIZAMI_CONFIG_FILE)
        saveResource(LEVEL_CONFIG_FILE)
        saveResource(PROJECTILE_CONFIG_FILE)
        saveResource(RARITY_CONFIG_FILE)
        saveResource("renderer.yml")
        saveResource(SKIN_CONFIG_FILE)
    }

    private fun registerListeners() = with(plugin) {
        registerTerminableListener(get<TestListener>()).bindWith(this)
    }

    /**
     * Starts the pre-world initialization process.
     */
    private fun initPreWorld() {
        initConfig()
        registerEvents() // register `this` listener
        registerListeners()

        fun forEachPreWorld(block: Initializable.() -> Unit): Result<Unit> {
            toInitPreWorld.forEach { initializable ->
                try {
                    block(initializable)
                } catch (e: Exception) {
                    logger.error("An exception occurred during pre-world initialization. Shutting down the server...", e)
                    shutdown()
                    return Result.failure(e)
                }
            }
            return Result.success(Unit)
        }

        logger.info("[Initializer] onPreWorld - Start")
        forEachPreWorld { onPreWorld() }.onFailure { return }
        logger.info("[Initializer] onPreWorld - Complete")

        logger.info("[Initializer] onPrePack - Start")
        forEachPreWorld { onPrePack() }.onFailure { return }
        logger.info("[Initializer] onPrePack - Complete")

        // TODO generate resource pack

        logger.info("[Initializer] onPostPackPreWorld - Start")
        forEachPreWorld { onPostPackPreWorld() }.onFailure { return }
        logger.info("[Initializer] onPostPackPreWorld - Complete")

        preWorldInitialized = true
    }

    /**
     * Starts the post-world initialization process.
     */
    private suspend fun initPostWorld() {
        fun forEachPostWorld(block: Initializable.() -> Unit): Result<Unit> {
            toInitPostWorld.forEach { initializable ->
                try {
                    block(initializable)
                } catch (e: Exception) {
                    logger.error("An exception occurred during post-world initialization. Shutting down the server...", e)
                    shutdown()
                    return Result.failure(e)
                }
            }
            return Result.success(Unit)
        }

        logger.info("[Initializer] onPostWorld - Start")
        forEachPostWorld { onPostWorld() }.onFailure { return }
        logger.info("[Initializer] onPostWorld - Complete")

        logger.info("[Initializer] onPostWorldAsync - Start")
        val asyncContext = Dispatchers.IO + CoroutineName("Neko Initializer - Post World Async")
        val onPostWorldJobs = mutableListOf<Job>()
        val onPostWorldAsyncResult = forEachPostWorld {
            onPostWorldJobs += NEKO_PLUGIN.launch(asyncContext) {
                onPostWorldAsync()
            }
        }
        logger.info("[Initializer] onPostWorldAsync - Waiting")
        onPostWorldJobs.joinAll() // wait for all async jobs
        onPostWorldAsyncResult.onFailure { return }
        logger.info("[Initializer] onPostWorldAsync - Complete")

        logger.info("[Initializer] onPostPack - Start")
        forEachPostWorld { onPostPack() }.onFailure { return }
        logger.info("[Initializer] onPostPack - Complete")

        logger.info("[Initializer] onPostPackAsync - Start")
        val onPostPackJobs = mutableListOf<Job>()
        val onPostPackAsyncResult = forEachPostWorld {
            onPostPackJobs += NEKO_PLUGIN.launch(asyncContext) {
                onPostPackAsync()
            }
        }
        logger.info("[Initializer] onPostPackAsync - Waiting")
        onPostPackJobs.joinAll() // wait for all async jobs
        onPostPackAsyncResult.onFailure { return }
        logger.info("[Initializer] onPostPackAsync - Complete")

        isDone = true
        NEKO_PLUGIN.launch(asyncContext) {
            callEvent(NekoLoadDataEvent()) // call it async
        }

        logger.info(Component.text("Done loading").color(NamedTextColor.AQUA))
    }

    /**
     * Disables all [Initializable]'s in the reverse order that they were
     * initialized in.
     */
    fun disable() {
        unregisterEvents()
        terminables.closeAndReportException()
    }

    private fun shutdown() {
        logger.warn("Shutting down the server...")
        Bukkit.shutdown()
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private fun handleLogin(e: PlayerLoginEvent) {
        if (!isDone) {
            e.disallow(PlayerLoginEvent.Result.KICK_OTHER, Component.text("[Neko] Initialization not complete. Please wait."))
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private suspend fun handleServerStarted(e: ServerLoadEvent) {
        if (preWorldInitialized) {
            initPostWorld()
        } else logger.warn("Skipping post world initialization")
    }

    /**
     * @throws CircularDependencyException
     */
    private fun handleDependencies() {
        val preWorldGraph: MutableGraph<KClass<out Initializable>> = GraphBuilder.directed().allowsSelfLoops(false).build()
        val postWorldGraph: MutableGraph<KClass<out Initializable>> = GraphBuilder.directed().allowsSelfLoops(false).build()

        fun populateDependencyGraph(
            dependencies: Array<KClass<out Initializable>>,
            clazz: KClass<out Initializable>,
            graph: MutableGraph<KClass<out Initializable>>, // <- graph to be populated
            after: Boolean,
        ) {
            dependencies.forEach { dependency ->
                if (after) {
                    graph.putEdge(dependency, clazz) // clazz depends on dependency
                } else {
                    graph.putEdge(clazz, dependency) // dependency depends on clazz
                }
            }
        }

        getKoin().getAll<Initializable>().forEach { initializable ->
            val clazz = initializable::class
            clazz.findAnnotation<DependencyConfig>()?.let { config ->
                populateDependencyGraph(config.preWorldAfter, clazz, preWorldGraph, true)
                populateDependencyGraph(config.preWorldBefore, clazz, preWorldGraph, false)
                populateDependencyGraph(config.postWorldAfter, clazz, postWorldGraph, true)
                populateDependencyGraph(config.postWorldBefore, clazz, postWorldGraph, false)
            }
        }

        // 拓扑排序并更新列表
        val sortedPreWorldClasses = topologicalSort(preWorldGraph)
        val sortedPostWorldClasses = topologicalSort(postWorldGraph)
        toInitPreWorld.clear()
        toInitPostWorld.clear()
        sortedPreWorldClasses.forEach { clazz -> getKoin().get<Initializable>(clazz).let(toInitPreWorld::add) }
        sortedPostWorldClasses.forEach { clazz -> getKoin().get<Initializable>(clazz).let(toInitPostWorld::add) }

        // 按依赖顺序把所有的 Initializables 放到 CompositeTerminable 里以支持 Terminable.close()
        // Side note: CompositeTerminable 本来就会以 FILO 的顺序调用 Terminable.close() 因此这里不需要先 List.reverse()
        terminables.withAll(toInitPreWorld)
        terminables.withAll(toInitPostWorld)
    }

    /**
     * @throws CircularDependencyException
     */
    private fun topologicalSort(
        graph: Graph<KClass<out Initializable>>,
    ): List<KClass<out Initializable>> {
        val visited = mutableSetOf<KClass<out Initializable>>()
        val tempMarks = mutableSetOf<KClass<out Initializable>>()
        val pathStack = mutableListOf<KClass<out Initializable>>()
        val result = mutableListOf<KClass<out Initializable>>()

        for (node in graph.nodes()) {
            if (node !in visited) {
                detectCycleAndSort(node, graph, visited, tempMarks, pathStack, result)
            }
        }

        return result.reversed() // reverse to get the correct order
    }

    /**
     * @throws CircularDependencyException
     */
    private fun detectCycleAndSort(
        node: KClass<out Initializable>,
        graph: Graph<KClass<out Initializable>>,
        visited: MutableSet<KClass<out Initializable>>,
        tempMarks: MutableSet<KClass<out Initializable>>,
        pathStack: MutableList<KClass<out Initializable>>,
        result: MutableList<KClass<out Initializable>>,
    ) {
        if (node in tempMarks) {
            val cycleStartIndex = pathStack.indexOf(node)
            val cycle = pathStack.subList(cycleStartIndex, pathStack.size).joinToString(" -> ") { it.simpleName ?: "Unknown" }
            throw CircularDependencyException("Detected circular dependency: $cycle -> ${node.simpleName}")
        }
        if (node !in visited) {
            tempMarks.add(node)
            pathStack.add(node)
            for (successor in graph.successors(node)) {
                detectCycleAndSort(successor, graph, visited, tempMarks, pathStack, result)
            }
            pathStack.remove(node)
            tempMarks.remove(node)
            visited.add(node)
            result.add(node)
        }
    }

    private class CircularDependencyException(message: String) : RuntimeException(message)
}