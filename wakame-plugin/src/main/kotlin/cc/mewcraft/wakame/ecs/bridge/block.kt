package cc.mewcraft.wakame.ecs.bridge

import cc.mewcraft.wakame.ecs.ECS
import cc.mewcraft.wakame.ecs.MetadataKeys
import cc.mewcraft.wakame.ecs.component.BlockComponent
import cc.mewcraft.wakame.ecs.external.KoishEntity
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.util.event
import cc.mewcraft.wakame.util.metadata.Metadata
import org.bukkit.event.world.ChunkUnloadEvent

/**
 * 返回该 [BukkitBlock] 对应的 [KoishEntity].
 * 如果该 [BukkitBlock] 不存在对应的 [KoishEntity], 则会创建一个新的.
 * 如果该 [BukkitBlock] 已经存在对应的 [KoishEntity], 则会返回已经存在的.
 *
 * ### 生命周期
 * 当本函数返回后, 如果 [BukkitBlock] 所在的区块卸载, 那么与之对应的 [KoishEntity] 也将变为无效.
 */
fun BukkitBlock.toKoish(): KoishEntity {
    val metadataMap = Metadata.provide(this)
    val koishEntity = metadataMap.getOrPut(MetadataKeys.ECS_BUKKIT_BLOCK_ENTITY_ID) {
        KoishEntity(ECS.createEntity { it += BlockComponent(this@toKoish) })
    }
    return koishEntity
}

@Init(stage = InitStage.POST_WORLD)
internal object BukkitBlockBridge {

    @InitFun
    fun init() {
        registerListeners()
    }

    private fun registerListeners() {
        event<ChunkUnloadEvent> { event ->
            // TODO 将失效的 ECSEntity 移除 ( 调用 entity.remove() )
            //  从区块里获取所有存在对应ECSEntity的Block似乎不是一个非常好的实现
            //  更好的实现也许是在 ECSSystem 遍历时检查其 BukkitBlock 所在的区块是否已卸载
        }
    }

}