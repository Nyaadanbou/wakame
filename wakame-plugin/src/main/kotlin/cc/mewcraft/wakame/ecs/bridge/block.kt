package cc.mewcraft.wakame.ecs.bridge

import cc.mewcraft.wakame.ecs.Fleks
import cc.mewcraft.wakame.ecs.MetadataKeys
import cc.mewcraft.wakame.ecs.component.BukkitBlockComponent
import cc.mewcraft.wakame.util.metadata.Metadata

/**
 * 返回该 [BukkitBlock] 对应的 [KoishEntity].
 * 如果该 [BukkitBlock] 不存在对应的 [KoishEntity], 则会创建一个新的.
 * 如果该 [BukkitBlock] 已经存在对应的 [KoishEntity], 则会返回已经存在的.
 *
 * ### 生命周期
 * 当本函数返回后, 如果 [BukkitBlock] 所在的区块卸载, 那么与之对应的 [KoishEntity] 也将变为无效.
 */
fun BukkitBlock.koishify(): KoishEntity {
    val metadataMap = Metadata.provide(this)
    val koishEntity = metadataMap.getOrPut(MetadataKeys.ECS_BUKKIT_BLOCK_ENTITY_ID) {
        KoishEntity(Fleks.createEntity { it += BukkitBlockComponent(this@koishify) })
    }
    return koishEntity
}

// BukkitBlock 所对应的 ECS entity 将通过一个 ECS system 自动移除
