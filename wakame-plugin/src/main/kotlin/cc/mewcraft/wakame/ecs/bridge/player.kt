package cc.mewcraft.wakame.ecs.bridge

import cc.mewcraft.wakame.ecs.ECS
import cc.mewcraft.wakame.ecs.MetadataKeys
import cc.mewcraft.wakame.ecs.component.PlayerComponent
import cc.mewcraft.wakame.ecs.component.WithAbility
import cc.mewcraft.wakame.ecs.external.KoishEntity
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.util.event
import cc.mewcraft.wakame.util.metadata.Metadata
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
fun BukkitPlayer.toKoish(): KoishEntity {
    val metadataMap = Metadata.provide(this)
    val koishEntity = metadataMap.getOrPut(MetadataKeys.ECS_BUKKIT_PLAYER_ENTITY_ID) {
        KoishEntity(ECS.createEntity {
            it += PlayerComponent(this@toKoish)
            it += WithAbility()
        })
    }
    return koishEntity
}


@Init(stage = InitStage.POST_WORLD)
internal object BukkitPlayerBridge {

    @InitFun
    fun init() {
        registerListeners()
    }

    private fun registerListeners() {
        event<PlayerJoinEvent> { event ->
            event.player.toKoish()
        }

        event<PlayerQuitEvent> { event ->
            event.player.toKoish().invalidate()
        }
    }

}