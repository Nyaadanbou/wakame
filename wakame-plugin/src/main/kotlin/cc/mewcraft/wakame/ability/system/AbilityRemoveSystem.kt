package cc.mewcraft.wakame.ability.system

import cc.mewcraft.wakame.ability.component.AbilityArchetypeComponent
import cc.mewcraft.wakame.ability.component.AbilityComponent
import cc.mewcraft.wakame.ability.component.AbilityContainer
import cc.mewcraft.wakame.ability.component.CastBy
import cc.mewcraft.wakame.ability.component.TargetTo
import cc.mewcraft.wakame.ecs.Families
import cc.mewcraft.wakame.ecs.component.HoldBy
import cc.mewcraft.wakame.item.logic.ItemRecord
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem

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

        val itemRecord = entity[CastBy].caster.getOrNull(ItemRecord)
        if (itemRecord != null && entity.has(HoldBy)) {
            // 如果技能被一个物品持有, 则进行物品技能的移除逻辑.
            val holdItem = entity[HoldBy].nekoStack
            val slot = entity[HoldBy].slot
            if (itemRecord[slot] == holdItem.bukkitStack) {
                // 如果玩家的背包里的物品是技能所对应的物品, 则不进行移除.
                return
            }
        }

        // 非物品技能直接移除.
        removeAbilityEntity(entity)
    }

    private fun removeAbilityEntity(entity: Entity) {
        entity[CastBy].caster[AbilityContainer].remove(entity[AbilityArchetypeComponent].archetype, entity)
        entity.remove()
    }
}