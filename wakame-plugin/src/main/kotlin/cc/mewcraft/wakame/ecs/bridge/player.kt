package cc.mewcraft.wakame.ecs.bridge

import cc.mewcraft.wakame.ecs.ECS
import cc.mewcraft.wakame.ecs.MetadataKeys
import cc.mewcraft.wakame.ecs.component.BukkitPlayerComponent
import cc.mewcraft.wakame.ecs.external.KoishEntity
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.util.event
import cc.mewcraft.wakame.util.metadata.Metadata
import org.bukkit.event.EventPriority
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

/**
 * 返回该 [BukkitPlayer] 对应的 [KoishEntity].
 * 如果该 [BukkitPlayer] 不存在对应的 [KoishEntity], 则会创建一个新的.
 * 如果该 [BukkitPlayer] 已经存在对应的 [KoishEntity], 则会返回已经存在的.
 *
 * ### 生命周期
 * 当本函数返回后, 如果 [BukkitPlayer] 退出服务器, 那么与之对应的 [KoishEntity] 也将变为无效.
 */
fun BukkitPlayer.koishify(): KoishEntity {
    val metadataMap = Metadata.provide(this)
    val koishEntity = metadataMap.getOrPut(MetadataKeys.ECS_BUKKIT_PLAYER_ENTITY_ID) {
        KoishEntity(ECS.createEntity { it += BukkitPlayerComponent(this@koishify) })
    }
    return koishEntity
}

@Init(stage = InitStage.POST_WORLD)
private object BukkitPlayerBridge {

    @InitFun
    fun init() {
        registerListeners()
    }

    private fun registerListeners() {

        // 在玩家进入服务器时, 为他创建对应的 ECS Entity
        event<PlayerJoinEvent>(priority = EventPriority.LOWEST) { event ->
            event.player.koishify()
        }

        // 在玩家退出服务器时, 移除他对应的 ECS Entity
        event<PlayerQuitEvent> { event ->
            event.player.koishify().invalidate()
        }
    }

}