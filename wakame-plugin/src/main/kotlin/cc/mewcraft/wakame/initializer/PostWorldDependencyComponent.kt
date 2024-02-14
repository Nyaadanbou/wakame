package cc.mewcraft.wakame.initializer

import cc.mewcraft.wakame.dependency.DependencyComponent
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation

internal class PostWorldDependencyComponent(
    override val component: KClass<out Initializable>,
) : DependencyComponent<KClass<out Initializable>> {
    override val dependenciesBefore: List<KClass<out Initializable>> = component.findAnnotation<DependencyConfig>()?.postWorldBefore?.asList().orEmpty()
    override val dependenciesAfter: List<KClass<out Initializable>> = component.findAnnotation<DependencyConfig>()?.postWorldAfter?.asList().orEmpty()
}