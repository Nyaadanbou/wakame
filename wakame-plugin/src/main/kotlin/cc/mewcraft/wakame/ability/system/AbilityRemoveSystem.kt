package cc.mewcraft.wakame.ability.system

import cc.mewcraft.wakame.ability.component.*
import cc.mewcraft.wakame.ecs.Families
import cc.mewcraft.wakame.item.logic.ItemSlotChanges
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem

class AbilityRemoveSystem : IteratingSystem(
    family = Families.ABILITY
) {
    override fun onTickEntity(entity: Entity) {
        val abilityComponent = entity[AbilityComponent]
        val caster = entity[CastBy].caster
        val target = entity[TargetTo].target

        // 开发日记 25/3/6
        // 要判断一个生物是否在 world 中, 使用 World#contains(Entity)
        // 而不是 Entity#isMarkedForRemoval
        if (caster !in world || target !in world) {
            entity.remove()
            return
        }

        // 还是有效的技能才判断逻辑

        if (!abilityComponent.isReadyToRemove)
            return

        val slot = entity.getOrNull(AtSlot)?.slot
        if (slot != null && caster in Families.BUKKIT_PLAYER) {
            val itemSlotChanges = caster[ItemSlotChanges]
            // 如果技能被栏位持有, 则进行物品技能的移除逻辑.

            val itemSlotChangesEntry = itemSlotChanges[slot]
            if (!itemSlotChangesEntry.changing) {
                // 如果玩家栏位无变化, 则不进行移除.
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