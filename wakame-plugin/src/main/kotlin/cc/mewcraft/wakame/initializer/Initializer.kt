package cc.mewcraft.wakame.initializer

import cc.mewcraft.wakame.NEKO_PLUGIN
import cc.mewcraft.wakame.WakamePlugin
import cc.mewcraft.wakame.event.NekoLoadDataEvent
import cc.mewcraft.wakame.test.TestListener
import cc.mewcraft.wakame.util.callEvent
import cc.mewcraft.wakame.util.registerEvents
import cc.mewcraft.wakame.util.unregisterEvents
import com.github.shynixn.mccoroutine.bukkit.launch
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.joinAll
import me.lucko.helper.terminable.TerminableConsumer
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

object Initializer : KoinComponent, TerminableConsumer, Listener {

    private val logger: ComponentLogger by inject(mode = LazyThreadSafetyMode.NONE)
    private val plugin: WakamePlugin by inject(mode = LazyThreadSafetyMode.NONE)
    private val terminables: CompositeTerminable = CompositeTerminable.create()

    /**
     * Whether the pre-world initialization has been completed successfully.
     */
    private var preWorldInitialized = false

    var isDone: Boolean = false
        private set

    fun start() {
        initPreWorld()
    }

    private fun initConfig() = with(plugin) {
        saveDefaultConfig() // config.yml
        saveResourceRecursively("crates")
        saveResourceRecursively("items")
        saveResourceRecursively("skills")
        saveResource("attributes.yml")
        saveResource("elements.yml")
        saveResource("categories.yml")
        saveResource("kizami.yml")
        saveResource("levels.yml")
        saveResource("projectiles.yml")
        saveResource("rarities.yml")
        saveResource("renderer.yml")
        saveResource("skins.yml")
    }

    private fun registerListeners() = with(plugin) {
        registerTerminableListener(get<TestListener>()).bindWith(this)
    }

    private fun forEachInitializable(block: (Initializable) -> Unit): Boolean {
        for (initializable in getKoin().getAll<Initializable>()) {
            try {
                block(initializable)
            } catch (e: Exception) {
                logger.error("An exception occurred in ${Initializer::class.simpleName}", e)
                return false // SOME initializable failed
            }
        }

        return true // ALL initializables return
    }

    /**
     * Starts the pre-world initialization process.
     */
    private fun initPreWorld() {
        initConfig()
        registerEvents() // register `this` listener
        registerListeners()

        ////

        logger.info("[Initializer] onPreWorld - Start")
        if (!forEachInitializable(Initializable::onPreWorld)) {
            shutdown(); return
        }
        logger.info("[Initializer] onPreWorld - Complete")

        ////

        logger.info("[Initializer] onPrePack - Start")
        if (!forEachInitializable(Initializable::onPrePack)) {
            shutdown(); return
        }
        logger.info("[Initializer] onPrePack - Complete")

        ////

        // TODO generate resource pack

        ////

        logger.info("[Initializer] onPostPackPreWorld - Start")
        if (!forEachInitializable(Initializable::onPostPackPreWorld)) {
            shutdown(); return
        }
        logger.info("[Initializer] onPostPackPreWorld - Complete")

        ////

        preWorldInitialized = true
    }

    /**
     * Starts the post-world initialization process.
     */
    private suspend fun initPostWorld() {
        logger.info("[Initializer] onPostWorld - Start")
        if (!forEachInitializable(Initializable::onPostWorld)) {
            shutdown(); return
        }
        logger.info("[Initializer] onPostWorld - Complete")

        ////

        logger.info("[Initializer] onPostWorldAsync - Start")
        val onPostWorldJobs = mutableListOf<Job>()
        val onPostWorldAsyncResult = forEachInitializable { initializable ->
            onPostWorldJobs += NEKO_PLUGIN.launch(
                Dispatchers.IO + CoroutineName("Neko Initializer - Post World Async")
            ) {
                initializable.onPostWorldAsync()
            }
        }
        logger.info("[Initializer] onPostWorldAsync - Waiting")
        onPostWorldJobs.joinAll() // wait for all async jobs
        if (!onPostWorldAsyncResult) {
            shutdown(); return
        }
        logger.info("[Initializer] onPostWorldAsync - Complete")

        ////

        logger.info("[Initializer] onPostPack - Start")
        if (!forEachInitializable(Initializable::onPostPack)) {
            shutdown(); return
        }
        logger.info("[Initializer] onPostPack - Complete")

        ////

        logger.info("[Initializer] onPostPackAsync - Start")
        val context = Dispatchers.IO + CoroutineName("Neko Initializer - Post Pack Async")
        val onPostPackJobs = mutableListOf<Job>()
        val onPostPackAsyncResult = forEachInitializable { initializable ->
            onPostPackJobs += NEKO_PLUGIN.launch(context) {
                initializable.onPostPackAsync()
            }
        }
        logger.info("[Initializer] onPostPackAsync - Waiting")
        onPostPackJobs.joinAll() // wait for all async jobs
        if (!onPostPackAsyncResult) {
            shutdown(); return
        }
        logger.info("[Initializer] onPostPackAsync - Complete")

        ////

        isDone = true
        NEKO_PLUGIN.launch(context) {
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
        forEachInitializable(Initializable::closeAndReportException)
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

    override fun <T : AutoCloseable> bind(terminable: T): T {
        return terminables.bind(terminable)
    }

}