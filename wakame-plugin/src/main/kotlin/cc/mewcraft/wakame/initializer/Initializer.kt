@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.initializer

import cc.mewcraft.wakame.NEKO_PLUGIN
import cc.mewcraft.wakame.WakamePlugin
import cc.mewcraft.wakame.command.CommandManager
import cc.mewcraft.wakame.config.Configs
import cc.mewcraft.wakame.config.MAIN_CONFIG
import cc.mewcraft.wakame.config.entry
import cc.mewcraft.wakame.damage.DamageListener
import cc.mewcraft.wakame.damage.VanillaDamageMappings.DAMAGE_GLOBAL_CONFIG_FILE
import cc.mewcraft.wakame.dependency.CircularDependencyException
import cc.mewcraft.wakame.dependency.DependencyResolver
import cc.mewcraft.wakame.display.RENDERER_GLOBAL_CONFIG_FILE
import cc.mewcraft.wakame.event.NekoCommandReloadEvent
import cc.mewcraft.wakame.event.NekoPostLoadDataEvent
import cc.mewcraft.wakame.eventbus.PluginEventBus
import cc.mewcraft.wakame.item.ItemBehaviorListener
import cc.mewcraft.wakame.item.ItemChangeListener
import cc.mewcraft.wakame.item.ItemMiscellaneousListener
import cc.mewcraft.wakame.pack.ResourcePackLifecycleListener
import cc.mewcraft.wakame.pack.ResourcePackPlayerListener
import cc.mewcraft.wakame.packet.DamageDisplay
import cc.mewcraft.wakame.player.component.ComponentListener
import cc.mewcraft.wakame.player.equipment.ArmorChangeListener
import cc.mewcraft.wakame.player.interact.FuckOffHandListener
import cc.mewcraft.wakame.player.inventory.ItemSlotWatcher
import cc.mewcraft.wakame.reforge.mod.ModdingTableSerializer.REFORGE_DIR_NAME
import cc.mewcraft.wakame.registry.*
import cc.mewcraft.wakame.registry.KizamiRegistry.KIZAMI_DIR_NAME
import cc.mewcraft.wakame.user.PaperUserManager
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

    private val LOGGER: ComponentLogger by inject()
    private val PLUGIN: WakamePlugin by inject()

    /**
     * A registry of `Terminables`.
     */
    private val terminables: CompositeTerminable = CompositeTerminable.create()

    /**
     * A list containing all [Initializables][Initializable] instances for
     * `reload` stage in the order by which they should be executed.
     */
    private val toLateReload: MutableList<Initializable> = ArrayList()

    /**
     * A list containing all [Initializables][Initializable] instances for
     * `pre-world` stage in the order by which they should be executed.
     */
    private val toInitPreWorld: MutableList<Initializable> = ArrayList()

    /**
     * A list containing all [Initializables][Initializable] instances for
     * `post-world` stage in the order by which they should be executed.
     */
    private val toInitPostWorld: MutableList<Initializable> = ArrayList()

    /**
     * Whether the `pre-world` initialization has been completed successfully.
     */
    private var preWorldInitialized = false

    /**
     * Returns `true` if this server is in "debug mode". The server **will**
     * tolerate unhandled exceptions if the debug mode is on. That is, logging
     * the exceptions without shutting down the server. While the debug mode
     * is off, the server will simply be shutdown upon unhandled exceptions
     * to prevent further damage.
     */
    val isDebug: Boolean by MAIN_CONFIG.entry<Boolean>("debug")

    /**
     * Returns `true` if this initializer finishes up all expected
     * initialization.
     */
    var isDone: Boolean = false
        private set

    /**
     * Should be called before the world is loaded.
     */
    fun start() {
        handleDependencies()
        initPreWorld()
    }

    private fun saveDefaultConfiguration() = with(PLUGIN) {
        saveDefaultConfig() // config.yml
        saveResourceRecursively(CRATE_PROTO_CONFIG_DIR)
        saveResourceRecursively(ITEM_PROTO_CONFIG_DIR)
        saveResourceRecursively(KIZAMI_DIR_NAME)
        saveResourceRecursively(LANG_PROTO_CONFIG_DIR)
        saveResourceRecursively(REFORGE_DIR_NAME)
        saveResourceRecursively(SKILL_PROTO_CONFIG_DIR)
        saveResource(ATTRIBUTE_GLOBAL_CONFIG_FILE)
        // saveResource(CATEGORY_GLOBAL_CONFIG_FILE) // 完成该模块后再去掉注释
        saveResource(DAMAGE_GLOBAL_CONFIG_FILE)
        saveResource(ELEMENT_GLOBAL_CONFIG_FILE)
        saveResource(ENTITY_GLOBAL_CONFIG_FILE)
        saveResource(ITEM_GLOBAL_CONFIG_FILE)
        saveResource(LEVEL_GLOBAL_CONFIG_FILE)
        // saveResource(PROJECTILE_GLOBAL_CONFIG_FILE) // 完成该模块后再去掉注释
        saveResource(RARITY_GLOBAL_CONFIG_FILE)
        saveResource(RENDERER_GLOBAL_CONFIG_FILE)
        // saveResource(SKIN_GLOBAL_CONFIG_FILE) // 完成该模块后再去掉注释
    }

    private fun registerListeners() = with(PLUGIN) {
        registerTerminableListener(get<ArmorChangeListener>()).bindWith(this)
        registerTerminableListener(get<ComponentListener>()).bindWith(this)
        registerTerminableListener(get<FuckOffHandListener>()).bindWith(this)
        registerTerminableListener(get<ItemChangeListener>()).bindWith(this)
        registerTerminableListener(get<DamageDisplay>()).bindWith(this)
        registerTerminableListener(get<PaperUserManager>()).bindWith(this)
        registerTerminableListener(get<ResourcePackLifecycleListener>()).bindWith(this)
        registerTerminableListener(get<ResourcePackPlayerListener>()).bindWith(this)
        registerTerminableListener(get<ItemBehaviorListener>()).bindWith(this)
        registerTerminableListener(get<ItemMiscellaneousListener>()).bindWith(this)
        registerTerminableListener(get<DamageListener>()).bindWith(this)
        registerTerminableListener(get<ItemSlotWatcher>()).bindWith(this)
    }

    private fun registerCommands() {
        CommandManager(PLUGIN).init()
    }

    private suspend fun executeReload() {
        fun forEachReload(block: Initializable.() -> Unit): Result<Unit> {
            toLateReload.forEach { initializable ->
                try {
                    block(initializable)
                } catch (e: Exception) {
                    shutdown("An exception occurred during reload initialization.", e)
                    return Result.failure(e)
                }
            }
            return Result.success(Unit)
        }

        LOGGER.info("[Initializer] onReload - Start")
        forEachReload { onReload() }.onFailure { return }
        LOGGER.info("[Initializer] onReload - Complete")

        val asyncContext = Dispatchers.IO + CoroutineName("Neko Initializer - Reload Async")
        val onReloadAsyncJobs = mutableListOf<Job>()
        val onReloadAsyncResult = forEachReload {
            onReloadAsyncJobs += NEKO_PLUGIN.launch(asyncContext) { onReloadAsync() }
        }
        LOGGER.info("[Initializer] onReloadAsync - Waiting")
        onReloadAsyncJobs.joinAll() // wait for all async jobs
        onReloadAsyncResult.onFailure { return }
        LOGGER.info("[Initializer] onReloadAsync - Complete")
    }

    /**
     * Starts the `pre-world` initialization process.
     */
    private fun initPreWorld() {
        saveDefaultConfiguration()
        registerEvents() // register `this` listener
        registerListeners()
        registerCommands()

        fun forEachPreWorld(block: Initializable.() -> Unit): Result<Unit> {
            toInitPreWorld.forEach { initializable ->
                try {
                    block(initializable)
                } catch (e: Exception) {
                    shutdown("An exception occurred during pre-world initialization.", e)
                    return Result.failure(e)
                }
            }
            return Result.success(Unit)
        }

        LOGGER.info("[Initializer] onPreWorld - Start")
        forEachPreWorld { onPreWorld() }.onFailure { return }
        LOGGER.info("[Initializer] onPreWorld - Complete")

        preWorldInitialized = true
    }

    /**
     * Starts the `post-world` initialization process.
     */
    private suspend fun initPostWorld() {
        fun forEachPostWorld(block: Initializable.() -> Unit): Result<Unit> {
            toInitPostWorld.forEach { initializable ->
                try {
                    block(initializable)
                } catch (e: Exception) {
                    shutdown("An exception occurred during post-world initialization.", e)
                    return Result.failure(e)
                }
            }
            return Result.success(Unit)
        }

        LOGGER.info("[Initializer] onPostWorld - Start")
        forEachPostWorld { onPostWorld() }.onFailure { return }
        LOGGER.info("[Initializer] onPostWorld - Complete")

        LOGGER.info("[Initializer] onPostWorldAsync - Start")
        val asyncContext = Dispatchers.IO + CoroutineName("Neko Initializer - Post World Async")
        val onPostWorldJobs = mutableListOf<Job>()
        val onPostWorldAsyncResult = forEachPostWorld {
            onPostWorldJobs += NEKO_PLUGIN.launch(asyncContext) { onPostWorldAsync() }
        }
        LOGGER.info("[Initializer] onPostWorldAsync - Waiting")
        onPostWorldJobs.joinAll() // wait for all async jobs
        onPostWorldAsyncResult.onFailure { return }
        LOGGER.info("[Initializer] onPostWorldAsync - Complete")

        isDone = true
        NEKO_PLUGIN.launch(asyncContext) {
            NekoPostLoadDataEvent().run {
                this.callEvent() // call it async
                PluginEventBus.get().post(this)
            }
        }

        LOGGER.info(Component.text("Done loading", NamedTextColor.AQUA))
    }

    /**
     * Disables all [Initializable]'s in the reverse order that they were
     * initialized in.
     */
    fun disable() {
        unregisterEvents()
        terminables.closeAndReportException()
    }

    private fun shutdown(message: String, throwable: Throwable) {
        if (!isDebug) {
            LOGGER.error(message, throwable)
            LOGGER.error("Shutting down the server to prevent further damage.")
            Bukkit.shutdown()
        } else {
            LOGGER.warn(message, throwable)
            LOGGER.warn("Scroll up to find the exception cause and check your configuration.")
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST) // we want to have final say, so handle it the latest
    private fun handleLogin(e: PlayerLoginEvent) {
        if (!isDone && !isDebug) {
            e.disallow(PlayerLoginEvent.Result.KICK_OTHER, Component.text("[萌芽] Initialization not complete. Please wait."))
        }
    }

    @EventHandler(priority = EventPriority.LOWEST) // resourcepack is reloaded the earliest
    private suspend fun handleServerStarted(e: ServerLoadEvent) {
        if (preWorldInitialized) {
            initPostWorld()
        } else {
            LOGGER.warn("Skipping post world initialization")
        }
    }

    @EventHandler(priority = EventPriority.LOWEST) // configs are reloaded the earliest
    private suspend fun handlePluginReloaded(e: NekoCommandReloadEvent) {
        Configs.reload()
        executeReload()
    }

    /**
     * @throws CircularDependencyException
     */
    private fun handleDependencies() {
        // Get all initializes from the Koin container
        val reloadComponents = getKoin().getAll<Initializable>().map { ReloadDependencyComponent(it::class) }
        val preWorldComponents = getKoin().getAll<Initializable>().map { PreWorldDependencyComponent(it::class) }
        val postWorldComponents = getKoin().getAll<Initializable>().map { PostWorldDependencyComponent(it::class) }

        // Sort the initializables by their dependency config
        val sortedReloadClasses = DependencyResolver.resolveDependencies(reloadComponents)
        val sortedPreWorldClasses = DependencyResolver.resolveDependencies(preWorldComponents)
        val sortedPostWorldClasses = DependencyResolver.resolveDependencies(postWorldComponents)

        // Add them to the lists
        toLateReload.clear()
        toInitPreWorld.clear()
        toInitPostWorld.clear()
        sortedReloadClasses.forEach { toLateReload.add(getKoin().get(it)) }
        sortedPreWorldClasses.forEach { toInitPreWorld.add(getKoin().get(it)) }
        sortedPostWorldClasses.forEach { toInitPostWorld.add(getKoin().get(it)) }

        // Side note:
        // CompositeTerminable 本来就会以 FILO 的顺序调用 Terminable.close()
        // 因此这里按照加载的顺序添加 Terminable 就好，不需要先 List.reverse()
        terminables.withAll(toLateReload)
        terminables.withAll(toInitPreWorld)
        terminables.withAll(toInitPostWorld)
    }
}