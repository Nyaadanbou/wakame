package cc.mewcraft.wakame.integration

import cc.mewcraft.wakame.BootstrapContexts
import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.api.protection.ProtectionIntegration
import cc.mewcraft.wakame.config.MAIN_CONFIG
import cc.mewcraft.wakame.config.entry
import cc.mewcraft.wakame.entity.player.ResourceLoadingFixHandler
import cc.mewcraft.wakame.integration.economy.EconomyIntegration
import cc.mewcraft.wakame.integration.economy.EconomyManager
import cc.mewcraft.wakame.integration.economy.EconomyType
import cc.mewcraft.wakame.integration.permission.PermissionIntegration
import cc.mewcraft.wakame.integration.permission.PermissionManager
import cc.mewcraft.wakame.integration.playerlevel.PlayerLevelIntegration
import cc.mewcraft.wakame.integration.playerlevel.PlayerLevelManager
import cc.mewcraft.wakame.integration.playerlevel.PlayerLevelType
import cc.mewcraft.wakame.integration.playermana.PlayerManaIntegration
import cc.mewcraft.wakame.integration.playermana.PlayerManaType
import cc.mewcraft.wakame.integration.protection.ProtectionManager
import cc.mewcraft.wakame.integration.townflight.TownFlightIntegration
import cc.mewcraft.wakame.integration.townflight.TownFlightManager
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.util.data.JarUtils
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.objectweb.asm.Type
import kotlin.reflect.KClass

@Init(stage = InitStage.PRE_FLEKS)
internal object HooksLoader {

    /**
     * 用户指定的经济账户系统.
     */
    private val ECONOMY_PROVIDER by MAIN_CONFIG.entry<EconomyType>("economy_provider")

    /**
     * 用户指定的玩家等级系统.
     */
    private val PLAYER_LEVEL_PROVIDER by MAIN_CONFIG.entry<PlayerLevelType>("player_level_provider")

    /**
     * 用户指定的玩家魔法值系统.
     */
    private val PLAYER_MANA_PROVIDER by MAIN_CONFIG.entry<PlayerManaType>("player_mana_provider")

    @InitFun
    fun init() {
        loadHooks()
    }

    @Suppress("UNCHECKED_CAST")
    private fun loadHooks() {
        val pluginJar = BootstrapContexts.PLUGIN_JAR.toFile()
        JarUtils.findAnnotatedClasses(
            pluginJar,
            listOf(Hook::class), listOf(),
            "cc/mewcraft/wakame/hook/impl/"
        ).classes[Hook::class]?.forEach { (className, annotations) ->
            val annotation = annotations.first()
            try {
                val plugins = (annotation["plugins"] as? List<String>) ?: emptyList()
                val unless = (annotation["unless"] as? List<String>) ?: emptyList()
                val requireAll = (annotation["requireAll"] as? Boolean) == true
                val loadListener = annotation["loadListener"] as? Type

                if (plugins.isEmpty()) {
                    throw IllegalStateException("Hook annotation on $className does not specify any plugins")
                }

                if (shouldLoadHook(plugins, unless, requireAll)) {
                    loadHook(className.replace('/', '.'), loadListener)
                    LOGGER.info(Component.text("Hook ${className.substringAfterLast('/')} loaded").color(NamedTextColor.AQUA))
                }
            } catch (t: Throwable) {
                LOGGER.error("Failed to load hook $className", t)
            }
        }
    }

    private fun shouldLoadHook(plugins: List<String>, unless: List<String>, requireAll: Boolean): Boolean {
        if (plugins.isEmpty()) {
            throw IllegalStateException("No plugins specified")
        }

        val pluginManager = Bukkit.getPluginManager()
        return if (requireAll) {
            plugins.all { pluginManager.getPlugin(it) != null } && unless.none { pluginManager.getPlugin(it) != null }
        } else {
            plugins.any { pluginManager.getPlugin(it) != null } && unless.none { pluginManager.getPlugin(it) != null }
        }
    }

    private fun loadHook(className: String, loadListener: Type?) {
        if (loadListener != null) {
            @Suppress("UNCHECKED_CAST")
            val obj = (Class.forName(loadListener.className).kotlin as KClass<out LoadListener>).objectInstance
            if (obj == null) throw IllegalStateException("$loadListener is not an object class")

            if (!obj.loaded.get()) { // blocking call
                return
            }
        }

        val hookClass = Class.forName(className).kotlin
        val hookInstance = hookClass.objectInstance
        if (hookInstance == null) throw IllegalStateException("$hookClass is not an object class")

        useHook(hookInstance)
    }

    private fun useHook(hook: Any) {
        if (hook is EconomyIntegration && ECONOMY_PROVIDER == hook.type) {
            EconomyManager.integration = hook // overwrite
        }
        if (hook is PermissionIntegration) {
            PermissionManager.integrations += hook
        }
        if (hook is ResourceLoadingFixHandler) {
            ResourceLoadingFixHandler.CURRENT_HANDLER = hook
        }
        if (hook is PlayerLevelIntegration && PLAYER_LEVEL_PROVIDER == hook.levelType) {
            PlayerLevelManager.integration = hook // overwrite
        }
        if (hook is PlayerManaIntegration && PLAYER_MANA_PROVIDER == hook.manaType) {
            PlayerManaIntegration.setImplementation(hook)
        }
        if (hook is ProtectionIntegration) {
            ProtectionManager.integrations += hook
        }
        if (hook is TownFlightIntegration) {
            TownFlightManager.integration = hook
        }
    }
}