package cc.mewcraft.wakame

import cc.mewcraft.wakame.ability.abilityModule
import cc.mewcraft.wakame.adventure.adventureModule
import cc.mewcraft.wakame.command.KoishCommandManager
import cc.mewcraft.wakame.config.Configs
import cc.mewcraft.wakame.config.PermanentStorage
import cc.mewcraft.wakame.entity.entityModule
import cc.mewcraft.wakame.gui.guiModule
import cc.mewcraft.wakame.lifecycle.initializer.Initializer
import cc.mewcraft.wakame.pack.packModule
import cc.mewcraft.wakame.registry.registryModule
import cc.mewcraft.wakame.util.data.Version
import cc.mewcraft.wakame.util.data.VersionRange
import cc.mewcraft.wakame.world.worldModule
import io.papermc.paper.plugin.bootstrap.BootstrapContext
import io.papermc.paper.plugin.bootstrap.PluginBootstrap
import io.papermc.paper.plugin.bootstrap.PluginProviderContext
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.debug.DebugProbes
import net.kyori.adventure.text.logger.slf4j.ComponentLogger
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.core.LoggerContext
import org.bukkit.plugin.java.JavaPlugin
import org.koin.core.context.GlobalContext.startKoin
import java.nio.file.Path
import kotlin.reflect.KProperty

private val REQUIRED_SERVER_VERSION: VersionRange = Version("1.21.3")..Version("1.21.3")
internal val PREVIOUS_KOISH_VERSION: Version? = PermanentStorage.retrieveOrNull<Version>("last_version")
internal val IS_DEV_SERVER: Boolean = System.getProperty("KoishDev") != null

internal lateinit var BOOTSTRAPPER: KoishBootstrapper private set
internal lateinit var LIFECYCLE_MANAGER: LifecycleEventManager<*>

lateinit var KOISH_NAME: String private set
lateinit var KOISH_VERSION: Version private set
lateinit var KOISH_JAR: Path private set

@PublishedApi
internal val KOISH_SCOPE = CoroutineScope(CoroutineName("Koish") + SupervisorJob())

@get:JvmName("getLogger")
val LOGGER: ComponentLogger by KoishLoggerProvider

internal class KoishBootstrapper : PluginBootstrap {
    init {
        BOOTSTRAPPER = this
    }

    override fun bootstrap(context: BootstrapContext) {
        startKoin {
            modules(
                // submodules (按字母顺序)
                adventureModule(),
                entityModule(),
                guiModule(),
                packModule(),
                registryModule(),
                abilityModule(),
                worldModule(),
            )
        }

        LIFECYCLE_MANAGER = context.lifecycleManager
        KoishLoggerProvider.set(context.logger)
        KOISH_NAME = context.pluginMeta.name
        KOISH_VERSION = Version(context.pluginMeta.version)
        KOISH_JAR = context.pluginSource

        if (IS_DEV_SERVER) {
            LOGGER.warn("Running in dev mode! Never use this on a production server!")
        }

        // prevent the plugin from starting on an unsupported server version
        if (Version.SERVER_VERSION !in REQUIRED_SERVER_VERSION) {
            throw Exception(
                """
                Koish is not compatible with this version of Minecraft.
                Koish v$KOISH_VERSION only runs on $REQUIRED_SERVER_VERSION.
                """.trimIndent()
            )
        }

        // prevent execution if the previously installed version is not compatible with this version
        // if (PREVIOUS_KOISH_VERSION != null && PREVIOUS_KOISH_VERSION < Version("x.y.z")) {
        //     throw Exception("This version of Koish is not compatible with the version that was previously installed.\n" +
        //             "Please erase all data related to Koish and try again.")
        // }

        // initialize commands
        KoishCommandManager.bootstrap(context)

        init()
    }

    override fun createPlugin(context: PluginProviderContext): JavaPlugin {
        return Koish
    }

    private fun init() {
        try {
            if (PREVIOUS_KOISH_VERSION == null || PREVIOUS_KOISH_VERSION != Version("0.0.1-snapshot")) {
                LegacyDataMigrator.migrate()
            }

            if (IS_DEV_SERVER) {
                DebugProbes.install()
                DebugProbes.enableCreationStackTraces = true
            }

            Configs.extractDefaultConfig()
            Initializer.start()

        } catch (e: Exception) {
            LOGGER.error("", e)
            (LogManager.getContext(false) as LoggerContext).stop() // flush log messages
            Runtime.getRuntime().halt(-1) // force-quit
        }
    }
}

private object KoishLoggerProvider {
    private var LOGGER: ComponentLogger? = null

    init {
        if (SharedConstants.isRunningInIde) {
            // 单元测试使用专门的 Logger, 服务端上则由 PluginBootstrap#bootstrap 分配
            set(ComponentLogger.logger("KoishTest"))
        }
    }

    fun set(logger: ComponentLogger) {
        LOGGER = logger
    }

    fun get(): ComponentLogger {
        return LOGGER ?: error("Koish logger not initialized!")
    }

    operator fun getValue(thisRef: Any?, property: KProperty<*>?): ComponentLogger = get()
}