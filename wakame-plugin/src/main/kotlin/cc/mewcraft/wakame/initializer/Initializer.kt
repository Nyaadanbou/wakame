@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.initializer

import cc.mewcraft.wakame.NEKO_PLUGIN
import cc.mewcraft.wakame.WakamePlugin
import cc.mewcraft.wakame.dependency.CircularDependencyException
import cc.mewcraft.wakame.dependency.DependencyResolver
import cc.mewcraft.wakame.event.NekoLoadDataEvent
import cc.mewcraft.wakame.registry.*
import cc.mewcraft.wakame.test.TestListener
import cc.mewcraft.wakame.util.callEvent
import cc.mewcraft.wakame.util.registerEvents
import cc.mewcraft.wakame.util.unregisterEvents
import com.github.shynixn.mccoroutine.bukkit.launch
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
                    logger.info("${initializable::class.simpleName} start")
                    block(initializable)
                    logger.info("${initializable::class.simpleName} done")
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
                    logger.info("${initializable::class.simpleName} start")
                    block(initializable)
                    logger.info("${initializable::class.simpleName} done")
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
        val preWorldComponents = getKoin().getAll<Initializable>().map { initializable -> PreWorldDependencyComponent(initializable::class) }
        val postWorldComponents = getKoin().getAll<Initializable>().map { initializable -> PostWorldDependencyComponent(initializable::class) }

        // Sort the initializables by their dependency config
        val sortedPreWorldClasses = DependencyResolver.resolveDependencies(preWorldComponents)
        val sortedPostWorldClasses = DependencyResolver.resolveDependencies(postWorldComponents)
        // Add them to the lists
        toInitPreWorld.clear()
        toInitPostWorld.clear()
        sortedPreWorldClasses.forEach { clazz ->
            toInitPreWorld.add(getKoin().get(clazz))
        }
        sortedPostWorldClasses.forEach { clazz ->
            toInitPostWorld.add(getKoin().get(clazz))
        }

        // Side note:
        // CompositeTerminable 本来就会以 FILO 的顺序调用 Terminable.close()
        // 因此这里按照加载的顺序添加 Terminable 就好，不需要先 List.reverse()
        terminables.withAll(toInitPreWorld)
        terminables.withAll(toInitPostWorld)
    }
}