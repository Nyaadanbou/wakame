package cc.mewcraft.wakame.integration

import cc.mewcraft.wakame.BootstrapContexts
import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.api.protection.ProtectionIntegration
import cc.mewcraft.wakame.integration.permission.PermissionIntegration
import cc.mewcraft.wakame.integration.permission.PermissionManager
import cc.mewcraft.wakame.integration.protection.ProtectionManager
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
            val objectInstance = (Class.forName(loadListener.className).kotlin as KClass<out LoadAwaiter>).objectInstance
                ?: throw IllegalStateException("$loadListener is not an object class")
            if (!objectInstance.loaded.get()) { // blocking call
                return
            }
        }
        val hookClass = Class.forName(className).kotlin
        val hookInstance = hookClass.objectInstance
            ?: throw IllegalStateException("$hookClass is not an object class")
        useHook(hookInstance)
    }

    private fun useHook(hook: Any) {
        if (hook is PermissionIntegration) {
            PermissionManager.addImplementation(hook)
        }
        if (hook is ProtectionIntegration) {
            ProtectionManager.addImplementation(hook)
        }
    }
}