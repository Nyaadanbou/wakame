package cc.mewcraft.wakame.ecs.bridge

import cc.mewcraft.wakame.ecs.Fleks
import cc.mewcraft.wakame.ecs.MetadataKeys
import cc.mewcraft.wakame.ecs.component.BukkitObject
import cc.mewcraft.wakame.ecs.component.BukkitPlayer
import cc.mewcraft.wakame.util.metadata.Metadata
import org.bukkit.entity.Player

/**
 * 返回该 [BukkitPlayer] 对应的 [KoishEntity].
 * 如果该 [BukkitPlayer] 不存在对应的 [KoishEntity], 则会创建一个新的.
 * 如果该 [BukkitPlayer] 已经存在对应的 [KoishEntity], 则会返回已经存在的.
 *
 * ### 生命周期
 * 当本函数返回后, 如果 [BukkitPlayer] 退出服务器, 那么与之对应的 [KoishEntity] 也将变为无效.
 */
fun Player.koishify(): KoishEntity {
    val metadataMap = Metadata.provide(this)
    val koishEntity = metadataMap.getOrPut(MetadataKeys.ECS_BUKKIT_PLAYER_ENTITY_ID) {
        KoishEntity(
            Fleks.INSTANCE.createEntity {
                it += BukkitPlayer(this@koishify)
                it += BukkitObject
            }
        )
    }
    return koishEntity
}