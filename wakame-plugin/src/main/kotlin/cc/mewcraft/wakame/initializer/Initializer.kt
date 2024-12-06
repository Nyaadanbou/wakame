@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.initializer

import cc.mewcraft.wakame.NEKO
import cc.mewcraft.wakame.WakamePlugin
import cc.mewcraft.wakame.api.event.NekoLoadDataEvent
import cc.mewcraft.wakame.attribute.AttributeMapPatchListener
import cc.mewcraft.wakame.command.CommandManager
import cc.mewcraft.wakame.config.Configs
import cc.mewcraft.wakame.config.MAIN_CONFIG
import cc.mewcraft.wakame.damage.DamageListener
import cc.mewcraft.wakame.damage.DamagePostListener
import cc.mewcraft.wakame.dependency.CircularDependencyException
import cc.mewcraft.wakame.dependency.DependencyResolver
import cc.mewcraft.wakame.ecs.EcsListener
import cc.mewcraft.wakame.event.NekoCommandReloadEvent
import cc.mewcraft.wakame.eventbus.PluginEventBus
import cc.mewcraft.wakame.gui.GuiManager
import cc.mewcraft.wakame.item.ItemBehaviorListener
import cc.mewcraft.wakame.item.ItemChangeListener
import cc.mewcraft.wakame.item.ItemMiscellaneousListener
import cc.mewcraft.wakame.item.component.ItemComponentRegistry
import cc.mewcraft.wakame.item.logic.ItemSlotChangeManager
import cc.mewcraft.wakame.pack.ResourcePackLifecycleListener
import cc.mewcraft.wakame.pack.ResourcePackPlayerListener
import cc.mewcraft.wakame.packet.DamageDisplay
import cc.mewcraft.wakame.player.equipment.ArmorChangeEventSupport
import cc.mewcraft.wakame.registry.ATTRIBUTE_GLOBAL_CONFIG_FILE
import cc.mewcraft.wakame.registry.CRATE_PROTO_CONFIG_DIR
import cc.mewcraft.wakame.registry.ELEMENT_GLOBAL_CONFIG_FILE
import cc.mewcraft.wakame.registry.ENTITY_GLOBAL_CONFIG_FILE
import cc.mewcraft.wakame.registry.ITEM_PROTO_CONFIG_DIR
import cc.mewcraft.wakame.registry.KizamiRegistry.KIZAMI_DIR_NAME
import cc.mewcraft.wakame.registry.LANG_PROTO_CONFIG_DIR
import cc.mewcraft.wakame.registry.LEVEL_GLOBAL_CONFIG_FILE
import cc.mewcraft.wakame.registry.RARITY_GLOBAL_CONFIG_FILE
import cc.mewcraft.wakame.registry.SKILL_PROTO_CONFIG_DIR
import cc.mewcraft.wakame.resource.ResourceSynchronizer
import cc.mewcraft.wakame.skill2.SkillListener
import cc.mewcraft.wakame.user.PaperUserManager
import cc.mewcraft.wakame.user.PlayerLevelListener
import cc.mewcraft.wakame.util.registerSuspendingEvents
import cc.mewcraft.wakame.world.entity.BetterArmorStandListener
import cc.mewcraft.wakame.world.player.death.PlayerDeathProtect
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
import xyz.xenondevs.invui.InvUI
import xyz.xenondevs.invui.window.WindowManager

/**
 * @see Initializable
 */
object Initializer : KoinComponent, Listener {

    private val logger: ComponentLogger by inject()
    private val plugin: WakamePlugin by inject()

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
        warmupStaticInitializer()
        handleDependencies()
        initPreWorld()
    }

    private fun saveDefaultConfiguration() = with(plugin) {
        saveDefaultConfig() // config.yml
        saveResourceRecursively(CRATE_PROTO_CONFIG_DIR)
        saveResourceRecursively(ITEM_PROTO_CONFIG_DIR)
        saveResourceRecursively(KIZAMI_DIR_NAME)
        saveResourceRecursively(LANG_PROTO_CONFIG_DIR)
        saveResourceRecursively("reforge")
        saveResourceRecursively(SKILL_PROTO_CONFIG_DIR)
        saveResource(ATTRIBUTE_GLOBAL_CONFIG_FILE)
        // saveResource(CATEGORY_GLOBAL_CONFIG_FILE) // 完成该模块后再去掉注释
        saveResource(ELEMENT_GLOBAL_CONFIG_FILE)
        saveResource(ENTITY_GLOBAL_CONFIG_FILE)
        saveResource(ItemComponentRegistry.CONFIG_FILE_NAME)
        saveResource(LEVEL_GLOBAL_CONFIG_FILE)
        // saveResource(PROJECTILE_GLOBAL_CONFIG_FILE) // 完成该模块后再去掉注释
        saveResource(RARITY_GLOBAL_CONFIG_FILE)
        saveResourceRecursively("renderers")
        saveResourceRecursively("station")
        saveResourceRecursively("damage")
        // saveResource(SKIN_GLOBAL_CONFIG_FILE) // 完成该模块后再去掉注释
    }

    private fun registerListeners() {
        // item
        registerListener<ArmorChangeEventSupport>()
        registerListener<ItemSlotChangeManager>()
        registerListener<ItemChangeListener>()
        registerListener<ItemBehaviorListener>()
        registerListener<ItemMiscellaneousListener>()

        // attribute
        registerListener<AttributeMapPatchListener>()

        // damage
        registerListener<DamageListener>()
        registerListener<DamagePostListener>()
        registerListener<DamageDisplay>()

        // ecs
        registerListener<EcsListener>()

        // rpg player
        registerListener<PaperUserManager>()

        // skill
        registerListener<SkillListener>()

        // resourcepack
        registerListener<ResourcePackLifecycleListener>()
        registerListener<ResourcePackPlayerListener>()

        // game world
        registerListener<BetterArmorStandListener>()
        registerListener<PlayerDeathProtect>()

        // compatibility
        registerListener<PlayerLevelListener>("AdventureLevel")

        // uncategorized
    }

    private inline fun <reified T : Listener> registerListener(requiredPlugin: String? = null) {
        if (requiredPlugin != null) {
            if (plugin.isPluginPresent(requiredPlugin)) {
                plugin.registerListener(get<T>())
            } else {
                logger.info("Plugin $requiredPlugin is not present. Skipping listener ${T::class.simpleName}")
            }
        } else {
            plugin.registerListener(get<T>())
        }
    }

    private fun registerCommands() {
        CommandManager(plugin).init()
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

        logger.info("[Initializer] onReload - Start")
        forEachReload { onReload() }.onFailure { return }
        logger.info("[Initializer] onReload - Complete")

        val asyncContext = Dispatchers.IO + CoroutineName("Neko Initializer - Reload Async")
        val onReloadAsyncJobs = mutableListOf<Job>()
        val onReloadAsyncResult = forEachReload {
            onReloadAsyncJobs += NEKO.launch(asyncContext) { onReloadAsync() }
        }
        logger.info("[Initializer] onReloadAsync - Waiting")
        onReloadAsyncJobs.joinAll() // wait for all async jobs
        onReloadAsyncResult.onFailure { return }
        logger.info("[Initializer] onReloadAsync - Complete")
    }

    /**
     * Starts the `pre-world` initialization process.
     */
    private fun initPreWorld() {
        saveDefaultConfiguration()
        registerSuspendingEvents() // register `this` listener
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

        logger.info("[Initializer] onPreWorld - Start")
        forEachPreWorld { onPreWorld() }.onFailure { return }
        logger.info("[Initializer] onPreWorld - Complete")

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

        logger.info("[Initializer] onPostWorld - Start")
        forEachPostWorld { onPostWorld() }.onFailure { return }
        logger.info("[Initializer] onPostWorld - Complete")

        logger.info("[Initializer] onPostWorldAsync - Start")
        val asyncContext = Dispatchers.IO + CoroutineName("Neko Initializer - Post World Async")
        val onPostWorldJobs = mutableListOf<Job>()
        val onPostWorldAsyncResult = forEachPostWorld {
            onPostWorldJobs += NEKO.launch(asyncContext) { onPostWorldAsync() }
        }
        logger.info("[Initializer] onPostWorldAsync - Waiting")
        onPostWorldJobs.joinAll() // wait for all async jobs
        onPostWorldAsyncResult.onFailure { return }
        logger.info("[Initializer] onPostWorldAsync - Complete")

        isDone = true

        val event = NekoLoadDataEvent()
        event.callEvent()
        PluginEventBus.get().post(event)

        logger.info(Component.text("Done loading", NamedTextColor.AQUA))
    }

    /**
     * Disables all [Initializable]'s in the reverse order that they were
     * initialized in.
     */
    fun disable() {
        // 关闭服务器时服务端不会触发任何事件,
        // 需要我们手动执行保存玩家资源的逻辑.
        // 如果服务器有使用 HuskSync, 我们的插件必须在 HuskSync 之前关闭,
        // 否则 PDC 无法保存到 HuskSync 的数据库, 导致玩家资源数据丢失.
        ResourceSynchronizer.saveAll()

        // 关闭所有打开的 GUI
        GuiManager.closeAll()

        // 按顺序关闭所有的 Terminable
        terminables.closeAndReportException()
    }

    private fun shutdown(message: String, throwable: Throwable) {
        if (!isDebug) {
            logger.error(message, throwable)
            logger.error("Shutting down the server to prevent further damage.")
            Bukkit.shutdown()
        } else {
            logger.warn(message, throwable)
            logger.warn("Scroll up to find the exception cause and check your configuration.")
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
            logger.warn("Skipping post world initialization")
        }
    }

    @EventHandler(priority = EventPriority.LOWEST) // configs are reloaded the earliest
    private suspend fun handlePluginReloaded(e: NekoCommandReloadEvent) {
        Configs.reload()
        executeReload()
    }

    private fun warmupStaticInitializer() {
        InvUI.getInstance()
        WindowManager.getInstance()
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