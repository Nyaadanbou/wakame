package cc.mewcraft.wakame.ecs.bridge

import cc.mewcraft.wakame.ecs.ECS
import cc.mewcraft.wakame.ecs.MetadataKeys
import cc.mewcraft.wakame.ecs.component.BukkitEntityComponent
import cc.mewcraft.wakame.ecs.external.KoishEntity
import cc.mewcraft.wakame.util.metadata.Metadata
import org.bukkit.entity.Player

/**
 * 返回该 [BukkitEntity] 对应的 [KoishEntity].
 * 如果该 [BukkitEntity] 不存在对应的 [KoishEntity], 则会创建一个新的.
 * 如果该 [BukkitEntity] 已经存在对应的 [KoishEntity], 则会返回已经存在的.
 *
 * ### 生命周期
 * 当本函数返回后, 如果 [BukkitEntity] 变为`无效`, 那么与之对应的 [KoishEntity] 也将变为无效.
 */
fun BukkitEntity.koishify(): KoishEntity {
    if (this is Player) error("Do not call BukkitEntity.koishify() if the concrete object is a BukkitPlayer. Use BukkitPlayer.koishify() instead.")
    if (!this.isValid) error("Failed to get the corresponding KoishEntity since the BukkitEntity is no longer valid. See BukkitEntity.isValid().")
    val metadataMap = Metadata.provide(this)
    val koishEntity = metadataMap.getOrPut(MetadataKeys.ECS_BUKKIT_ENTITY_ENTITY_ID) {
        KoishEntity(ECS.createEntity { it += BukkitEntityComponent(this@koishify) })
    }
    return koishEntity
}
