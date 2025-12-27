package cc.mewcraft.wakame.integration

import java.util.concurrent.CompletableFuture
import kotlin.reflect.KClass

/**
 * An annotation to mark a class as a hook.
 *
 * @param plugins The names of the plugins that this hook works with.
 * @param unless The names of the plugins that this hook does not work with.
 * @param requireAll Whether all plugins in [plugins] have to be loaded for this hook to be loaded.
 * @param loadAwaiter The [LoadAwaiter] that is used to wait for the plugin to finish loading.
 */
@Target(AnnotationTarget.CLASS)
annotation class Hook(
    val plugins: Array<String>,
    val unless: Array<String> = [],
    val requireAll: Boolean = false,
    val loadAwaiter: KClass<out LoadAwaiter> = LoadAwaiter::class,
)

/**
 * A listener that is used to wait for a plugin to finish loading.
 */
interface LoadAwaiter {

    // 开发日记 2024/11/24 小米
    // 目前在 Nova 的实现中, 只有 ItemsAdder 用到了.
    /**
     * A [CompletableFuture] that is completed when the plugin is loaded or failed to load.
     */
    val loaded: CompletableFuture<Boolean>

}