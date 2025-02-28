package cc.mewcraft.wakame.lifecycle.reloader

import cc.mewcraft.wakame.lifecycle.LifecycleDispatcher
import kotlin.reflect.KClass

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class InternalReload(
    val dispatcher: LifecycleDispatcher = LifecycleDispatcher.SYNC,
    val dependsOn: Array<KClass<*>> = []
)

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class Reload(
    val dispatcher: LifecycleDispatcher = LifecycleDispatcher.SYNC,
    val runAfter: Array<KClass<*>> = [],
    val runBefore: Array<KClass<*>> = [],
)

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class ReloadFun(
    val dispatcher: LifecycleDispatcher = LifecycleDispatcher.SYNC,
    val runAfter: Array<KClass<*>> = [],
    val runBefore: Array<KClass<*>> = []
)