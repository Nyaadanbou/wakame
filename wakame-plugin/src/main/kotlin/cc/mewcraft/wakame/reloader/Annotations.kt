package cc.mewcraft.wakame.reloader

import cc.mewcraft.wakame.initializer2.Dispatcher
import kotlin.reflect.KClass

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class InternalReload(
    val dispatcher: Dispatcher = Dispatcher.SYNC,
    val dependsOn: Array<KClass<*>> = []
)

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class Reload(
    val order: ReloadableOrder,
    val dispatcher: Dispatcher = Dispatcher.SYNC,
    val runAfter: Array<KClass<*>> = [],
    val runBefore: Array<KClass<*>> = [],
)

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class ReloadableFun(
    val dispatcher: Dispatcher = Dispatcher.SYNC,
    val runAfter: Array<KClass<*>> = [],
    val runBefore: Array<KClass<*>> = []
)