package cc.mewcraft.wakame

import cc.mewcraft.wakame.ability.abilityModule
import cc.mewcraft.wakame.adventure.adventureModule
import cc.mewcraft.wakame.attribute.attributeModule
import cc.mewcraft.wakame.config.Configs
import cc.mewcraft.wakame.config.PermanentStorage
import cc.mewcraft.wakame.core.coreModule
import cc.mewcraft.wakame.craftingstation.stationModule
import cc.mewcraft.wakame.crate.crateModule
import cc.mewcraft.wakame.damage.damageModule
import cc.mewcraft.wakame.display2.display2Module
import cc.mewcraft.wakame.ecs.ecsModule
import cc.mewcraft.wakame.element.elementModule
import cc.mewcraft.wakame.enchantment.enchantmentModule
import cc.mewcraft.wakame.entity.entityModule
import cc.mewcraft.wakame.gui.guiModule
import cc.mewcraft.wakame.initializer2.Initializer
import cc.mewcraft.wakame.integration.integrationModule
import cc.mewcraft.wakame.item.itemModule
import cc.mewcraft.wakame.lang.langModule
import cc.mewcraft.wakame.pack.packModule
import cc.mewcraft.wakame.packet.packetModule
import cc.mewcraft.wakame.random3.randomModule
import cc.mewcraft.wakame.recipe.recipeModule
import cc.mewcraft.wakame.reforge.reforgeModule
import cc.mewcraft.wakame.registry.registryModule
import cc.mewcraft.wakame.resource.resourceModule
import cc.mewcraft.wakame.skin.skinModule
import cc.mewcraft.wakame.user.userModule
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

private val REQUIRED_SERVER_VERSION: VersionRange = Version("1.21.3")..Version("1.21.3")

internal val IS_DEV_SERVER: Boolean = System.getProperty("KoishDev") != null
internal val PREVIOUS_KOISH_VERSION: Version? = PermanentStorage.retrieveOrNull<Version>("last_version")

internal lateinit var BOOTSTRAPPER: KoishBootstrapper private set
internal lateinit var LIFECYCLE_MANAGER: LifecycleEventManager<*>

lateinit var KOISH_VERSION: Version private set
lateinit var KOISH_JAR: Path private set

internal val KOISH_SCOPE = CoroutineScope(CoroutineName("Koish") + SupervisorJob())

@JvmField // 避免函数调用的成本
val LOGGER: ComponentLogger = KoishLoggerHolder.get()

private object KoishLoggerHolder {
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
}

internal class KoishBootstrapper : PluginBootstrap {
    init {
        BOOTSTRAPPER = this
    }

    override fun bootstrap(context: BootstrapContext) {
        startKoin {
            modules(
                // main module
                koishModule(),

                // sub modules (按字母顺序)
                adventureModule(),
                attributeModule(),
                coreModule(),
                crateModule(),
                damageModule(),
                display2Module(),
                ecsModule(),
                elementModule(),
                enchantmentModule(),
                entityModule(),
                guiModule(),
                integrationModule(),
                itemModule(),
                langModule(),
                packetModule(),
                packModule(),
                randomModule(),
                recipeModule(),
                reforgeModule(),
                registryModule(),
                resourceModule(),
                skinModule(),
                abilityModule(),
                stationModule(),
                userModule(),
                worldModule(),
            )
        }

        LIFECYCLE_MANAGER = context.lifecycleManager
        KoishLoggerHolder.set(context.logger)
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

        init()
    }

    override fun createPlugin(context: PluginProviderContext): JavaPlugin {
        return Koish
    }

    private fun init() {
        try {
            if (PREVIOUS_KOISH_VERSION == null) {
                LegacyDataMigrator.migrate()
            }

            if (PREVIOUS_KOISH_VERSION != null && PREVIOUS_KOISH_VERSION != Version("0.0.1-snapshot")) {
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