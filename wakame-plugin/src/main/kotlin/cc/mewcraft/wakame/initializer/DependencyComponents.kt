package cc.mewcraft.wakame.initializer

import cc.mewcraft.wakame.dependency.DependencyComponent
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation

internal class PostWorldDependencyComponent(
    override val component: KClass<out Initializable>,
) : DependencyComponent<KClass<out Initializable>> {
    override val dependenciesBefore: List<KClass<out Initializable>> = component.findAnnotation<PostWorldDependency>()?.runBefore?.asList().orEmpty()
    override val dependenciesAfter: List<KClass<out Initializable>> = component.findAnnotation<PostWorldDependency>()?.runAfter?.asList().orEmpty()
}

internal class PreWorldDependencyComponent(
    override val component: KClass<out Initializable>,
) : DependencyComponent<KClass<out Initializable>> {
    override val dependenciesBefore: List<KClass<out Initializable>> = component.findAnnotation<PreWorldDependency>()?.runBefore?.asList().orEmpty()
    override val dependenciesAfter: List<KClass<out Initializable>> = component.findAnnotation<PreWorldDependency>()?.runAfter?.asList().orEmpty()
}

internal class ReloadDependencyComponent(
    override val component: KClass<out Initializable>,
) : DependencyComponent<KClass<out Initializable>> {
    override val dependenciesBefore: List<KClass<out Initializable>> = component.findAnnotation<ReloadDependency>()?.runBefore?.asList().orEmpty()
    override val dependenciesAfter: List<KClass<out Initializable>> = component.findAnnotation<ReloadDependency>()?.runAfter?.asList().orEmpty()
}