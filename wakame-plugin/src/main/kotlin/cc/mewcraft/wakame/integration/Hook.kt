package cc.mewcraft.wakame.integration

import java.util.concurrent.CompletableFuture
import kotlin.reflect.KClass

/**
 * Defines the stage at which a hook should be loaded.
 */
enum class HookStage {
    /**
     * The hook is loaded during the `onLoad` phase.
     */
    PRE_WORLD,

    /**
     * The hook is loaded during the `onEnable` phase.
     *
     * This is the default stage.
     */
    POST_WORLD,
}

/**
 * An annotation to mark a class as a hook.
 *
 * @param plugins The names of the plugins that this hook works with.
 * @param unless The names of the plugins that this hook does not work with.
 * @param requireAll Whether all plugins in [plugins] have to be loaded for this hook to be loaded.
 * @param stage The [HookStage] at which this hook should be loaded. Defaults to [HookStage.POST_WORLD].
 * @param loadAwaiter The [LoadAwaiter] that is used to wait for the plugin to finish loading.
 */
@Target(AnnotationTarget.CLASS)
annotation class Hook(
    val plugins: Array<String>,
    val unless: Array<String> = [],
    val requireAll: Boolean = false,
    val stage: HookStage = HookStage.POST_WORLD,
    val loadAwaiter: KClass<out LoadAwaiter> = LoadAwaiter::class,
)

/**
 * A listener that is used to wait for a plugin to finish loading.
 */
interface LoadAwaiter {

    /**
     * A [CompletableFuture] that is completed when the plugin is loaded or failed to load.
     */
    val loaded: CompletableFuture<Boolean>

}