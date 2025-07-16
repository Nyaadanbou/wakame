package cc.mewcraft.wakame

import cc.mewcraft.wakame.command.KoishCommandManager
import cc.mewcraft.wakame.config.ConfigAccess
import cc.mewcraft.wakame.config.Configs
import cc.mewcraft.wakame.config.PermanentStorage
import cc.mewcraft.wakame.lang.LanguageExtractor
import cc.mewcraft.wakame.lifecycle.initializer.Initializer
import cc.mewcraft.wakame.pack.AssetExtractor
import cc.mewcraft.wakame.util.data.Version
import cc.mewcraft.wakame.util.data.VersionRange
import io.papermc.paper.plugin.bootstrap.BootstrapContext
import io.papermc.paper.plugin.bootstrap.PluginBootstrap
import io.papermc.paper.plugin.bootstrap.PluginProviderContext
import kotlinx.coroutines.debug.DebugProbes
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.core.LoggerContext
import org.bukkit.plugin.java.JavaPlugin

private val REQUIRED_SERVER_VERSION: VersionRange = Version("1.21.7")..Version("1.21.7")
internal val PREVIOUS_KOISH_VERSION: Version? = PermanentStorage.retrieveOrNull<Version>("last_version")

internal class KoishBootstrap : PluginBootstrap {
    init {
        BootstrapContexts.registerBootstrap(this)
    }

    // See: https://docs.papermc.io/paper/dev/getting-started/paper-plugins#bootstrapper
    //
    // 该函数被调用的时机非常早, 会在以下关键时机之前被调用:
    // 1) 加载 NMS 的 classes (因此可以在这里对服务端代码进行 patching)
    // 2) 创建 JavaPlugin 实例 (因此可以直接用 object 来实现 JavaPlugin)
    override fun bootstrap(context: BootstrapContext) {
        LoggerProvider.set(context.logger)
        ConfigAccess.register(Configs) // 配置文件 API 实例趁早注册

        BootstrapContexts.registerLifecycleManagerOwnedByBootstrap(context.lifecycleManager)
        BootstrapContexts.registerAuthors(context.pluginMeta.authors)
        BootstrapContexts.registerName(context.pluginMeta.name)
        BootstrapContexts.registerVersion(Version(context.pluginMeta.version))
        BootstrapContexts.registerPluginJar(context.pluginSource)

        if (BootstrapContexts.IS_DEV_SERVER) {
            LOGGER.warn("Running in dev mode! Never use this on a production server!")
        }

        // prevent the plugin from starting on an unsupported server version
        if (Version.SERVER_VERSION !in REQUIRED_SERVER_VERSION) {
            throw Exception(
                """
                Koish is not compatible with this version of Minecraft.
                Koish v${BootstrapContexts.PLUGIN_VERSION} only runs on $REQUIRED_SERVER_VERSION.
                """.trimIndent()
            )
        }

        // prevent execution if the previously installed version is not compatible with this version
        // if (PREVIOUS_KOISH_VERSION != null && PREVIOUS_KOISH_VERSION < Version("x.y.z")) {
        //     throw Exception("This version of Koish is not compatible with the version that was previously installed.\n" +
        //             "Please erase all data related to Koish and try again.")
        // }

        try {
            // 初始化 Koish 所使用的路径
            KoishDataPaths.initialize()

            if (PREVIOUS_KOISH_VERSION == null || PREVIOUS_KOISH_VERSION != Version("0.0.1-snapshot")) {
                LegacyDataMigrator.migrate()
            }

            if (BootstrapContexts.IS_DEV_SERVER) {
                DebugProbes.install()
                DebugProbes.enableCreationStackTraces = true
            }

            // 配置文件必须最先初始化, 因为一般来说 Configs[...] 的返回值(下面称配置)都会赋值到 top-level 的 val,
            // 也就是说这些配置会随着 class 被 classloader 加载时直接实例化,
            // 而这些配置所对应的文件可能还没有内容 (例如首次使用 Koish 插件时数据文件还未被拷贝到插件的数据目录),
            // 从而导致读取配置项时找不到需要的配置项, 抛出 NPE
            Configs.initialize()
            LanguageExtractor.extractDefaults()
            AssetExtractor.extractDefaults()

            // 初始化所有 InitFun (PRE_WORLD)
            Initializer.initialize()
            Initializer.performPreWorld()

            // 让指令注册发生在所有 PRE_WORLD 的 InitFun 之后,
            // 这样如果之前发生了异常那么指令将不会注册,
            // 以避免执行指令所造成的二次伤害
            KoishCommandManager.bootstrap(context)

        } catch (e: Exception) {
            LOGGER.error("", e)
            (LogManager.getContext(false) as LoggerContext).stop() // flush log messages
            Runtime.getRuntime().halt(-1) // force-quit
        }
    }

    override fun createPlugin(context: PluginProviderContext): JavaPlugin {
        return KoishPlugin // 利用 Kotlin 的 object 特性来直接访问我们的 JavaPlugin 实例
    }
}
