package cc.mewcraft.wakame.ecs.bridge

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.ecs.Fleks
import cc.mewcraft.wakame.ecs.MetadataKeys
import cc.mewcraft.wakame.ecs.component.BukkitEntity
import cc.mewcraft.wakame.ecs.component.BukkitObject
import cc.mewcraft.wakame.ecs.component.BukkitPlayer
import cc.mewcraft.wakame.util.metadata.Metadata
import org.bukkit.entity.Entity
import org.bukkit.entity.Player

/**
 * 返回该 [BukkitEntity] 对应的 [KoishEntity].
 * 如果该 [BukkitEntity] 不存在对应的 [KoishEntity], 则会创建一个新的.
 * 如果该 [BukkitEntity] 已经存在对应的 [KoishEntity], 则会返回已经存在的.
 *
 * ### 生命周期
 * 当本函数返回后, 如果 [BukkitEntity] 变为`无效`, 那么与之对应的 [KoishEntity] 也将变为无效.
 */
fun Entity.koishify(): KoishEntity {
    val entity = this // make your life easier
    if (!entity.isValid && (entity !is Player || !entity.isConnected)) {
        // 对于玩家, 使用 isConnected 来判断是否创建 EEntity 更合适
        error("Failed to get the corresponding KoishEntity since the BukkitEntity is no longer valid.")
    }
    val metadataMap = Metadata.provide(entity)
    val koishEntity = metadataMap.getOrPut(MetadataKeys.ECS_BUKKIT_ENTITY_ENTITY_ID) {
        KoishEntity(
            Fleks.INSTANCE.createEntity {
                it += BukkitObject
                it += BukkitEntity(entity)
                if (entity is Player) {
                    it += BukkitPlayer(entity)
                    LOGGER.info("[ECS] Entity created for ${entity.name}")
                }
            }
        )
    }
    return koishEntity
}

// BukkitEntity 所对应的 ECS entity 将通过一个 ECS system 自动移除
