package cc.mewcraft.wakame

import cc.mewcraft.wakame.api.ApiItemRegistry
import cc.mewcraft.wakame.api.KoishProvider
import cc.mewcraft.wakame.api.block.BlockManager
import cc.mewcraft.wakame.api.block.KoishBlockRegistry
import cc.mewcraft.wakame.api.item.KoishItemRegistry
import cc.mewcraft.wakame.api.protection.ProtectionIntegration
import cc.mewcraft.wakame.api.tileentity.TileEntityManager
import cc.mewcraft.wakame.core.ItemXBootstrap
import cc.mewcraft.wakame.integration.protection.ProtectionManager
import cc.mewcraft.wakame.lifecycle.initializer.Initializer
import cc.mewcraft.wakame.util.ServerUtils
import cc.mewcraft.wakame.util.registerEvents
import kotlinx.coroutines.cancel
import me.lucko.helper.utils.JarExtractor
import org.bukkit.Bukkit
import org.bukkit.Server
import org.bukkit.plugin.java.JavaPlugin
import org.koin.core.context.stopKoin
import xyz.xenondevs.invui.InvUI
import java.io.File
import java.util.Objects.requireNonNull
import cc.mewcraft.wakame.api.Koish as IKoish

internal val SERVER: Server
    get() = Bukkit.getServer()
internal var PLUGIN_READY: Boolean = false
    private set

internal object Koish : JavaPlugin(), IKoish {

    override fun onEnable() {
        PLUGIN_READY = true
        LIFECYCLE_MANAGER = lifecycleManager

        InvUI.getInstance().setPlugin(this)
        Initializer.registerEvents()
        ItemXBootstrap.init()
        KoishProvider.register(this)
    }

    override fun onDisable() {
        KoishProvider.unregister()
        Initializer.disable()
        KOISH_SCOPE.cancel("Plugin disabled")
        stopKoin()

        if (ServerUtils.isReload()) {
            LOGGER.error("====================================================")
            LOGGER.error("RELOADING IS NOT SUPPORTED, SHUTTING DOWN THE SERVER")
            LOGGER.error("====================================================")
            Bukkit.shutdown()
        }
    }

    fun isPluginPresent(name: String): Boolean {
        return server.pluginManager.getPlugin(name) != null
    }

    fun getRelativeFile(name: String): File {
        dataFolder.mkdirs()
        return File(dataFolder, name)
    }

    fun getBundledFile(name: String): File {
        requireNonNull(name, "name")
        val file = getRelativeFile(name)
        if (!file.exists()) {
            saveResource(name, false)
        }
        return file
    }

    fun saveResource(name: String) {
        if (!getRelativeFile(name).exists()) {
            saveResource(name, false)
        }
    }

    fun saveResourceRecursively(name: String) {
        saveResourceRecursively(name, false)
    }

    fun saveResourceRecursively(name: String, overwrite: Boolean) {
        val targetDirectory = getRelativeFile(name)
        if (overwrite || !targetDirectory.exists()) {
            JarExtractor.extractJar(classLoader, name, targetDirectory)
        }
    }

    //<editor-fold desc="koish-api">
    override fun getTileEntityManager(): TileEntityManager? = TODO("Not yet implemented")
    override fun getBlockManager(): BlockManager? = TODO("Not yet implemented")
    override fun getBlockRegistry(): KoishBlockRegistry? = TODO("Not yet implemented")
    override fun getItemRegistry(): KoishItemRegistry? = ApiItemRegistry

    override fun registerProtectionIntegration(integration: ProtectionIntegration) {
        ProtectionManager.integrations += integration
    }
    //</editor-fold>
}