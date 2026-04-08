package cc.mewcraft.wakame.integration

import cc.mewcraft.wakame.KoishBootstrapContexts
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

@Init(InitStage.PRE_WORLD)
internal object PreWorldHooksLoader {

    @InitFun
    fun init() {
        HooksLoader.loadHooks(HookStage.PRE_WORLD)
    }
}

@Init(InitStage.POST_WORLD)
internal object PostWorldHooksLoader {

    @InitFun
    fun init() {
        HooksLoader.loadHooks(HookStage.POST_WORLD)
    }
}

private object HooksLoader {

    @Suppress("UNCHECKED_CAST")
    fun loadHooks(targetStage: HookStage) {
        val pluginJar = KoishBootstrapContexts.PLUGIN_JAR.toFile()
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
                val loadAwaiter = annotation["loadAwaiter"] as? Type
                val stage = resolveHookStage(annotation["stage"])
                if (plugins.isEmpty()) {
                    throw IllegalStateException("Hook annotation on $className does not specify any plugins")
                }
                if (stage != targetStage) {
                    return@forEach
                }
                if (shouldLoadHook(plugins, unless, requireAll)) {
                    loadHook(className.replace('/', '.'), loadAwaiter)
                    LOGGER.info(Component.text("Hook ${className.substringAfterLast('/')} loaded (${targetStage.name})").color(NamedTextColor.AQUA))
                }
            } catch (t: Throwable) {
                LOGGER.error("Failed to load hook $className", t)
            }
        }
    }

    /**
     * Resolves the [HookStage] from an ASM annotation enum value.
     *
     * ASM stores enum values as a `String[2]` array: `[descriptor, constantName]`.
     * If the annotation parameter is absent (i.e., using the default), returns [HookStage.POST_WORLD].
     */
    private fun resolveHookStage(value: Any?): HookStage {
        if (value is Array<*> && value.size == 2) {
            val name = value[1] as? String ?: return HookStage.POST_WORLD
            return HookStage.valueOf(name)
        }
        return HookStage.POST_WORLD
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

    private fun loadHook(className: String, loadAwaiter: Type?) {
        if (loadAwaiter != null) {
            val objectInstance = (Class.forName(loadAwaiter.className).kotlin as KClass<out LoadAwaiter>).objectInstance
                ?: throw IllegalStateException("$loadAwaiter is not an object class")
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