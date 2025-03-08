package cc.mewcraft.wakame.ability.system

import cc.mewcraft.wakame.ecs.Families
import cc.mewcraft.wakame.ecs.component.AbilityComponent
import cc.mewcraft.wakame.ecs.component.CastBy
import cc.mewcraft.wakame.ecs.component.HoldBy
import cc.mewcraft.wakame.ecs.component.TargetTo
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import org.bukkit.entity.Player

class AbilityRemoveSystem : IteratingSystem(
    family = Families.ABILITY
) {
    override fun onTickEntity(entity: Entity) {
        val abilityComponent = entity[AbilityComponent]
        val casterEntity = entity[CastBy].caster
        val targetEntity = entity[TargetTo].target

        // 开发日记 25/3/6
        // 要判断一个生物是否在 world 中, 使用 World#contains(Entity)
        // 而不是 Entity#isMarkedForRemoval
        if (casterEntity !in world || targetEntity !in world) {
            entity.remove()
            return
        }

        // 还是有效的技能才判断逻辑

        if (!abilityComponent.isReadyToRemove)
            return

        val caster = entity[CastBy].entityOrPlayer()
        if (caster is Player && entity.has(HoldBy)) {
            // 如果技能被一个物品持有, 则进行物品技能的移除逻辑.
            val holdItem = entity[HoldBy].nekoStack
            val slot = entity[HoldBy].slot
            if (slot.getItem(caster) == holdItem.bukkitStack) {
                // 如果玩家的背包里的物品是技能所对应的物品, 则不进行移除.
                return
            }
        }

        // 非物品技能直接移除.
        entity.remove()
    }
}