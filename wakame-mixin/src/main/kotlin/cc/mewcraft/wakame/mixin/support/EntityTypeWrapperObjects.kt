package cc.mewcraft.wakame.mixin.support

import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet

/**
 * 持有了所有已实例化的 [EntityTypeWrapper] 对象, 用于热更新 [EntityTypeWrapper.delegate].
 */
object EntityTypeWrapperObjects {

    private val instances: ReferenceOpenHashSet<EntityTypeWrapper<*>> = ReferenceOpenHashSet()

    fun register(instance: EntityTypeWrapper<*>) {
        instances.add(instance)
    }

    fun instances(): Set<EntityTypeWrapper<*>> {
        return instances
    }
}