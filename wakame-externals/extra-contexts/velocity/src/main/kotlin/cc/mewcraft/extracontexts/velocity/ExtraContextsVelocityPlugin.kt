package cc.mewcraft.extracontexts.velocity

import cc.mewcraft.extracontexts.api.KeyValueStoreContextProvider
import cc.mewcraft.extracontexts.api.KeyValueStoreManager
import cc.mewcraft.extracontexts.common.context.SimpleKeyValueStoreContextProvider
import cc.mewcraft.extracontexts.common.database.DatabaseManager
import cc.mewcraft.extracontexts.common.example.registerDummyKeyValuePairs
import cc.mewcraft.extracontexts.common.storage.SimpleKeyValueStoreManager
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

@Plugin(
    id = "extracontexts",
    name = "ExtraContexts",
    version = "0.0.1",
    authors = ["Nailm"],
    description = "Adds key-value pair context support to LuckPerms"
)
class ExtraContextsVelocityPlugin @Inject constructor(
    @param:DataDirectory
    private val dataDirectory: Path,
    private val logger: Logger,
) {
    private val luckPerms: LuckPerms
        get() = LuckPermsProvider.get()

    private lateinit var keyValueStoreManager: KeyValueStoreManager
    private lateinit var keyValueStoreContextProvider: KeyValueStoreContextProvider
    private lateinit var keyValueStoreContextCalculator: VelocityKeyValueStoreContextCalculator

    @Subscribe
    fun onProxyInitialize(event: ProxyInitializeEvent) {
        val configName = "config.yml"

        // Save default config.yml from resources to plugin directory if not exists
        saveResource(configName)

        // Load database configuration from file
        val configPath = dataDirectory.resolve(configName)
        val databaseConfig = ConfigurationLoader.loadConfiguration(configPath)

        // Initialize database
        DatabaseManager.initialize(databaseConfig)

        // Initialize managers
        keyValueStoreManager = SimpleKeyValueStoreManager
        keyValueStoreContextProvider = SimpleKeyValueStoreContextProvider()
        keyValueStoreContextCalculator = VelocityKeyValueStoreContextCalculator(keyValueStoreContextProvider)

        // Set implementation for static access
        KeyValueStoreManager.setImplementation(keyValueStoreManager)
        KeyValueStoreContextProvider.setImplementation(keyValueStoreContextProvider)

        // Register context calculator with LuckPerms
        luckPerms.contextManager.registerCalculator(keyValueStoreContextCalculator)

        // Register some dummy data
        registerDummyKeyValuePairs(logger)
    }

    @Subscribe
    fun onProxyShutdown(event: ProxyShutdownEvent) {
        luckPerms.contextManager.unregisterCalculator(keyValueStoreContextCalculator)
    }

    private fun saveResource(resourceName: String, overwrite: Boolean = false) {
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

