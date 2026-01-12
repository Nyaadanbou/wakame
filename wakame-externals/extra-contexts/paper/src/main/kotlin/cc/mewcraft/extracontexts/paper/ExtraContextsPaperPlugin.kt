package cc.mewcraft.extracontexts.paper

import cc.mewcraft.extracontexts.api.KeyValueStoreContextProvider
import cc.mewcraft.extracontexts.api.KeyValueStoreManager
import cc.mewcraft.extracontexts.common.context.SimpleKeyValueStoreContextProvider
import cc.mewcraft.extracontexts.common.database.DatabaseManager
import cc.mewcraft.extracontexts.common.example.registerDummyKeyValuePairs
import cc.mewcraft.extracontexts.common.storage.SimpleKeyValueStoreManager
import net.luckperms.api.LuckPerms
import net.luckperms.api.LuckPermsProvider
import org.bukkit.plugin.java.JavaPlugin

/**
 * ExtraContexts plugin for Paper.
 * Adds KVStore context support to LuckPerms.
 */
class ExtraContextsPaperPlugin : JavaPlugin() {

    private val luckPerms: LuckPerms
        get() = LuckPermsProvider.get()

    lateinit var keyValueStoreManager: KeyValueStoreManager
        private set
    lateinit var keyValueStoreContextProvider: KeyValueStoreContextProvider
        private set

    private lateinit var keyValueStoreContextCalculator: PaperKeyValueStoreContextCalculator

    override fun onEnable() {
        // Load database configuration from file
        val dbConfig = ConfigurationLoader.loadConfiguration(this)

        // Initialize database
        DatabaseManager.initialize(dbConfig)

        // Initialize managers
        keyValueStoreManager = SimpleKeyValueStoreManager
        keyValueStoreContextProvider = SimpleKeyValueStoreContextProvider()
        keyValueStoreContextCalculator = PaperKeyValueStoreContextCalculator(keyValueStoreContextProvider)

        // Set implementation for static access
        KeyValueStoreManager.setImplementation(keyValueStoreManager)
        KeyValueStoreContextProvider.setImplementation(keyValueStoreContextProvider)

        // Register context calculator with LuckPerms
        luckPerms.contextManager.registerCalculator(keyValueStoreContextCalculator)

        // Register some dummy data
        registerDummyKeyValuePairs(slF4JLogger)
    }

    override fun onDisable() {
        luckPerms.contextManager.unregisterCalculator(keyValueStoreContextCalculator)
    }
}

