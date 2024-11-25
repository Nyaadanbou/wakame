package cc.mewcraft.wakame.integration

import cc.mewcraft.wakame.Injector
import cc.mewcraft.wakame.NEKO_PLUGIN
import cc.mewcraft.wakame.api.protection.ProtectionIntegration
import cc.mewcraft.wakame.config.MAIN_CONFIG
import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.integration.economy.EconomyIntegration
import cc.mewcraft.wakame.integration.economy.EconomyManager
import cc.mewcraft.wakame.integration.economy.EconomyType
import cc.mewcraft.wakame.integration.permission.PermissionIntegration
import cc.mewcraft.wakame.integration.permission.PermissionManager
import cc.mewcraft.wakame.integration.playerlevel.PlayerLevelIntegration
import cc.mewcraft.wakame.integration.playerlevel.PlayerLevelManager
import cc.mewcraft.wakame.integration.playerlevel.PlayerLevelType
import cc.mewcraft.wakame.integration.protection.ProtectionManager
import cc.mewcraft.wakame.util.data.JarUtils
import org.bukkit.Bukkit
import org.koin.core.component.get
import org.objectweb.asm.Type
import org.slf4j.Logger
import kotlin.reflect.KClass

internal object HooksLoader : Initializable {

    /**
     * 用户指定的玩家等级系统.
     */
    private val selectedPlayerLevelIntegration by MAIN_CONFIG.entry<PlayerLevelType>("player_level_provider")

    /**
     * 用户指定的经济账户系统.
     */
    private val selectedEconomyIntegration by MAIN_CONFIG.entry<EconomyType>("economy_provider")

    // 部分 integration 会在 pre world 阶段加载默认(自带)的钩子,
    // 因此对于外部系统, 应该在 post world 阶段加载它们的钩子,
    // 这样的话外部的钩子实例就可以覆盖默认的钩子实例.
    override suspend fun onPostWorldAsync() {
        loadHooks()
    }

    @Suppress("UNCHECKED_CAST")
    private fun loadHooks() {
        JarUtils.findAnnotatedClasses(
            NEKO_PLUGIN.nekooJar,
            listOf(Hook::class), emptyList(),
            "xyz/xenondevs/nova/hook/impl/"
        ).classes[Hook::class]?.forEach { (className, annotations) ->
            val annotation = annotations.first()
            try {
                val plugins = (annotation["plugins"] as? List<String>) ?: emptyList()
                val unless = (annotation["unless"] as? List<String>) ?: emptyList()
                val requireAll = (annotation["requireAll"] as? Boolean) == true
                val loadListener = annotation["loadListener"] as? Type

                if (plugins.isEmpty())
                    throw IllegalStateException("hook annotation on $className does not specify any plugins")

                if (shouldLoadHook(plugins, unless, requireAll))
                    loadHook(className.replace('/', '.'), loadListener)
            } catch (t: Throwable) {
                Injector.get<Logger>().error("Failed to load hook $className", t)
            }
        }
    }

    private fun shouldLoadHook(plugins: List<String>, unless: List<String>, requireAll: Boolean): Boolean {
        if (plugins.isEmpty())
            throw IllegalStateException("no plugins specified")

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
                ?: throw IllegalStateException("the LoadListener $loadListener is not an object class")

            if (!obj.loaded.get())
                return
        }

        val hookClass = Class.forName(className).kotlin
        val hookInstance = hookClass.objectInstance
            ?: throw IllegalStateException("hook $hookClass is not an object class")

        useHook(hookInstance)
    }

    private fun useHook(hook: Any) {
        if (hook is EconomyIntegration && selectedEconomyIntegration == hook.type) {
            EconomyManager.integration = hook // overwrite
        }

        if (hook is PermissionIntegration) {
            PermissionManager.integrations += hook
        }

        if (hook is PlayerLevelIntegration && selectedPlayerLevelIntegration == hook.type) {
            PlayerLevelManager.integration = hook // overwrite
        }

        if (hook is ProtectionIntegration) {
            ProtectionManager.integrations += hook
        }
    }
}