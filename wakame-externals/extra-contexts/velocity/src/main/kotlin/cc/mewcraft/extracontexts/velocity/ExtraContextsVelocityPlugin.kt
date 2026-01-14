package cc.mewcraft.extracontexts.velocity

import cc.mewcraft.extracontexts.api.KeyValueStoreManager
import cc.mewcraft.extracontexts.common.database.DatabaseManager
import cc.mewcraft.extracontexts.common.database.ReactiveDatabaseConfiguration
import cc.mewcraft.extracontexts.common.example.registerDummyKeyValuePairs
import cc.mewcraft.extracontexts.common.messaging.MessagingInitializer
import cc.mewcraft.extracontexts.common.storage.CachedKeyValueStoreManager
import cc.mewcraft.lazyconfig.MAIN_CONFIG
import cc.mewcraft.messaging2.ReactiveMessagingConfiguration
import com.google.inject.Inject
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.plugin.annotation.DataDirectory
import net.luckperms.api.LuckPerms
import net.luckperms.api.LuckPermsProvider
import org.slf4j.Logger
import java.nio.file.Path
import kotlin.io.path.outputStream


const val PLUGIN_NAMESPACE = "extracontexts"

@Plugin(
    id = "extracontexts",
    name = "ExtraContexts",
    version = "0.0.1",
    authors = ["Nailm"],
    description = "Adds key-value pair context support to LuckPerms"
)
class ExtraContextsVelocityPlugin @Inject constructor(
    val logger: Logger,
    @param:DataDirectory
    val dataDirectory: Path,
) {
    private val luckPerms: LuckPerms
        get() = LuckPermsProvider.get()

    private lateinit var pluginConfigs: VelocityPluginConfigs
    private lateinit var keyValueStoreManager: KeyValueStoreManager
    private lateinit var keyValueStoreContextCalculator: VelocityKeyValueStoreContextCalculator

    @Subscribe
    fun onProxyInitialize(event: ProxyInitializeEvent) {
        pluginConfigs = VelocityPluginConfigs(this)
        pluginConfigs.initialize()

        // Initialize database
        DatabaseManager.initialize(ReactiveDatabaseConfiguration(MAIN_CONFIG))

        // Initialize messaging
        MessagingInitializer.initialize(ReactiveMessagingConfiguration(MAIN_CONFIG))

        // Initialize managers
        keyValueStoreManager = CachedKeyValueStoreManager
        keyValueStoreContextCalculator = VelocityKeyValueStoreContextCalculator(keyValueStoreManager)

        // Set implementation for static access
        KeyValueStoreManager.setImplementation(keyValueStoreManager)

        // Register context calculator with LuckPerms
        luckPerms.contextManager.registerCalculator(keyValueStoreContextCalculator)

        // Register some dummy data
        registerDummyKeyValuePairs(logger)
    }

    @Subscribe
    fun onProxyShutdown(event: ProxyShutdownEvent) {
        luckPerms.contextManager.unregisterCalculator(keyValueStoreContextCalculator)
    }

    fun saveResource(resourceName: String, overwrite: Boolean = false) {
        val outputPath = dataDirectory.resolve(resourceName)
        if (!overwrite && outputPath.toFile().exists()) {
            return
        }

        outputPath.parent?.toFile()?.mkdirs()

        javaClass.getResourceAsStream("/$resourceName").use { inputStream ->
            if (inputStream != null) {
                outputPath.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            } else {
                logger.warn("Resource file '$resourceName' not found in JAR")
            }
        }
    }
}
