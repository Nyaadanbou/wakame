package cc.mewcraft.wakame.initializer

import kotlin.reflect.KClass

/**
 * Configures the dependency resolution of [Initializable].
 *
 * Currently, only [Initializable.onPreWorld] and
 * [Initializable.onPostWorld] are supported.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
internal annotation class DependencyConfig(
    /**
     * [Initializable.onPreWorld] of the classes must run **after** this.
     */
    val preWorldAfter: Array<KClass<out Initializable>> = [],
    /**
     * [Initializable.onPreWorld] of the classes must run **before** `this`.
     */
    val preWorldBefore: Array<KClass<out Initializable>> = [],
    /**
     * [Initializable.onPostWorld] of the classes must run **after** `this`.
     */
    val postWorldAfter: Array<KClass<out Initializable>> = [],
    /**
     * [Initializable.onPostWorld] of the classes must run **before** `this`.
     */
    val postWorldBefore: Array<KClass<out Initializable>> = [],
)
