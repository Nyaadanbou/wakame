package cc.mewcraft.extracontexts.paper

import cc.mewcraft.extracontexts.api.KeyValueStoreManager
import cc.mewcraft.extracontexts.common.database.DatabaseManager
import cc.mewcraft.extracontexts.common.database.ReactiveDatabaseConfiguration
import cc.mewcraft.extracontexts.common.example.registerDummyKeyValuePairs
import cc.mewcraft.extracontexts.common.messaging.MessagingInitializer
import cc.mewcraft.extracontexts.common.storage.CachedKeyValueStoreManager
import cc.mewcraft.lazyconfig.MAIN_CONFIG
import cc.mewcraft.messaging2.ReactiveMessagingConfiguration
import net.luckperms.api.LuckPerms
import net.luckperms.api.LuckPermsProvider
import org.bukkit.plugin.java.JavaPlugin

const val PLUGIN_NAMESPACE = "extracontexts"

/**
 * ExtraContexts plugin for Paper.
 * Adds KVStore context support to LuckPerms.
 */
class ExtraContextsPaperPlugin : JavaPlugin() {

    private val luckPerms: LuckPerms
        get() = LuckPermsProvider.get()

    private lateinit var pluginConfigs: PaperPluginConfigs
    private lateinit var keyValueStoreManager: KeyValueStoreManager
    private lateinit var keyValueStoreContextCalculator: PaperKeyValueStoreContextCalculator

    override fun onEnable() {
        pluginConfigs = PaperPluginConfigs(this)
        pluginConfigs.initialize()

        // Initialize database
        DatabaseManager.initialize(ReactiveDatabaseConfiguration(MAIN_CONFIG))

        // Initialize messaging
        MessagingInitializer.initialize(ReactiveMessagingConfiguration(MAIN_CONFIG))

        // Initialize managers
        keyValueStoreManager = CachedKeyValueStoreManager
        keyValueStoreContextCalculator = PaperKeyValueStoreContextCalculator(keyValueStoreManager)

        // Set implementation for static access
        KeyValueStoreManager.setImplementation(keyValueStoreManager)

        // Register context calculator with LuckPerms
        luckPerms.contextManager.registerCalculator(keyValueStoreContextCalculator)

        // Register some dummy data
        registerDummyKeyValuePairs(slF4JLogger)
    }

    override fun onDisable() {
        luckPerms.contextManager.unregisterCalculator(keyValueStoreContextCalculator)
    }
}

