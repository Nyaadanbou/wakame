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
     * [Initializable.onPreWorld] of `this` should run **after** that of the
     * specified classes.
     */
    val preWorldAfter: Array<KClass<out Initializable>> = [],
    /**
     * [Initializable.onPreWorld] of `this` should run **before** that of the
     * specified classes.
     */
    val preWorldBefore: Array<KClass<out Initializable>> = [],
    /**
     * [Initializable.onPostWorld] of `this` should run **after** that of the
     * specified classes.
     */
    val postWorldAfter: Array<KClass<out Initializable>> = [],
    /**
     * [Initializable.onPostWorld] of `this` should run **before** that of the
     * specified classes.
     */
    val postWorldBefore: Array<KClass<out Initializable>> = [],
)
