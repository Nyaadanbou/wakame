package cc.mewcraft.wakame.ability.system

import cc.mewcraft.wakame.ecs.WakameWorld
import cc.mewcraft.wakame.ecs.component.AbilityComponent
import cc.mewcraft.wakame.ecs.component.CastBy
import cc.mewcraft.wakame.ecs.component.HoldBy
import cc.mewcraft.wakame.ecs.component.Tags
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family

class AbilityMechanicRemoveSystem : IteratingSystem(
    family = family { all(AbilityComponent, Tags.READY_TO_REMOVE) }
) {
    override fun onTickEntity(entity: Entity) {
        val casterEntity = entity.getOrNull(CastBy)?.caster?.player
        if (casterEntity != null && entity.has(HoldBy)) {
            // 如果技能被一个物品持有, 则进行物品技能的移除逻辑.
            val holdItem = entity[HoldBy].nekoStack
            val slot = entity[HoldBy].slot
            if (slot.getItem(casterEntity) == holdItem.bukkitStack) {
                // 如果玩家的背包里的物品是技能所对应的物品, 则不进行移除.
                return
            }
        }
        // 非物品技能直接移除.
        WakameWorld.removeEntity(entity)
    }
}