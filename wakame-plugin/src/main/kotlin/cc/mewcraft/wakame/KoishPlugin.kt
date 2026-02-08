package cc.mewcraft.wakame

import cc.mewcraft.wakame.api.ApiItemRegistry
import cc.mewcraft.wakame.api.KoishProvider
import cc.mewcraft.wakame.api.block.BlockManager
import cc.mewcraft.wakame.api.block.KoishBlockRegistry
import cc.mewcraft.wakame.api.item.KoishItemRegistry
import cc.mewcraft.wakame.api.protection.ProtectionIntegration
import cc.mewcraft.wakame.api.tileentity.TileEntityManager
import cc.mewcraft.wakame.integration.protection.ProtectionManager
import cc.mewcraft.wakame.lifecycle.initializer.Initializer
import cc.mewcraft.wakame.util.ServerUtils
import cc.mewcraft.wakame.util.data.JarUtils
import cc.mewcraft.wakame.util.registerEvents
import kotlinx.coroutines.cancel
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.util.Objects.requireNonNull
import cc.mewcraft.wakame.api.Koish as IKoish

internal object KoishPlugin : JavaPlugin(), IKoish {

    override fun onLoad() {
        PluginProvider.set(this)
        KoishProvider.register(this)
    }

    override fun onEnable() {
        BootstrapContexts.registerLifecycleManagerOwnedByPlugin(lifecycleManager) // LifecycleManager 此时已发生变化, 重新注册

        // 在 onEnable() 调用 Initializer.performPostWorld() 可以让我们利用 paper-plugin.yml
        // 中的插件依赖的配置项来控制 KoishPlugin#onEnable 的调用时机是先于其他插件还是晚于其他插件.
        // 这主要是可以让本项目的 hooks 的加载时机可以遵循 paper-plugin.yml 中定义的依赖.
        Initializer.performPostWorld()
        Initializer.registerEvents()

        BootstrapContexts.setPluginReady(true)
    }

    override fun onDisable() {
        BootstrapContexts.setPluginReady(false)
        KoishProvider.unregister()
        Initializer.performDisable()
        PLUGIN_SCOPE.cancel("Koish Plugin has been disabled")

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

    private fun getRelativeFile(name: String): File {
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
            JarUtils.extractJar(classLoader, name, targetDirectory)
        }
    }

    //<editor-fold desc="koish-api">
    override fun getTileEntityManager(): TileEntityManager = TODO("Not yet implemented")
    override fun getBlockManager(): BlockManager = TODO("Not yet implemented")
    override fun getBlockRegistry(): KoishBlockRegistry = TODO("Not yet implemented")
    override fun getItemRegistry(): KoishItemRegistry = ApiItemRegistry

    override fun registerProtectionIntegration(integration: ProtectionIntegration) {
        ProtectionManager.addImplementation(integration)
    }
    //</editor-fold>
}