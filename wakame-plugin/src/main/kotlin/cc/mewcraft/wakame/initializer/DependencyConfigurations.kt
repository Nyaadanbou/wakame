package cc.mewcraft.wakame.initializer

import kotlin.reflect.KClass


/**
 * Configures the dependency resolution of [Initializable].
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class PostWorldDependency(
    /**
     * Classes that must run **after** `this`.
     */
    val runAfter: Array<KClass<out Initializable>> = [],
    /**
     * Classes that must run **before** `this`.
     */
    val runBefore: Array<KClass<out Initializable>> = [],
)

/**
 * Configures the dependency resolution of [Initializable].
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class PreWorldDependency(
    /**
     * Classes that must run **after** `this`.
     */
    val runAfter: Array<KClass<out Initializable>> = [],
    /**
     * Classes that must run **before** `this`.
     */
    val runBefore: Array<KClass<out Initializable>> = [],
)

/**
 * Configures the dependency resolution of [Initializable].
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class ReloadDependency(
    /**
     * Classes that must run **after** `this`.
     */
    val runAfter: Array<KClass<out Initializable>> = [],
    /**
     * Classes that must run **before** `this`.
     */
    val runBefore: Array<KClass<out Initializable>> = [],
)
