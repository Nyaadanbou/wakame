package cc.mewcraft.wakame.mixin.support

import cc.mewcraft.wakame.LOGGER
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

    fun reloadInstances() {
        // 重新初始化 MythicBootstrapBridge
        MythicBootstrapBridge.init()

        // 更新所有 EntityTypeWrapper 的 delegate
        for (inst in EntityTypeWrapperObjects.instances()) {
            val id = inst.id
            val entityType = MythicBootstrapBridge.getEntityType(id) ?: run {
                LOGGER.warn("The MythicMobs entity '$id' has no corresponding EntityType")
                continue
            }
            inst.setDelegate(entityType)
        }
    }
}