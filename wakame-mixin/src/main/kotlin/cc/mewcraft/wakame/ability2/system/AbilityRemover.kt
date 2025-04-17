package cc.mewcraft.wakame.ability2.system

import cc.mewcraft.wakame.ability2.component.Ability
import cc.mewcraft.wakame.ability2.component.AbilityContainer
import cc.mewcraft.wakame.ability2.component.AtSlot
import cc.mewcraft.wakame.ability2.component.CastBy
import cc.mewcraft.wakame.ability2.component.TargetTo
import cc.mewcraft.wakame.ecs.Families
import cc.mewcraft.wakame.item2.ItemSlotChanges
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem

object AbilityRemover : IteratingSystem(
    family = Families.ABILITY
) {
    override fun onTickEntity(entity: Entity) {
        val ability = entity[Ability]
        val caster = entity[CastBy].caster
        val target = entity[TargetTo].target

        // 开发日记 25/3/6
        // 要判断一个生物是否在 world 中, 使用 World#contains(Entity)
        // 而不是 Entity#isMarkedForRemoval
        if (caster !in world || target !in world) {
            entity.remove()
            return
        }

        if (!ability.isReadyToRemove)
            return // 还是有效的技能才继续执行

        val slot = entity.getOrNull(AtSlot)?.slot
        if (slot != null && caster in Families.BUKKIT_PLAYER) {
            // 如果技能被栏位持有, 则进行物品技能的移除逻辑.
            val slotChanges = caster[ItemSlotChanges]
            val entry = slotChanges[slot]
            if (!entry.changing) {
                return // 如果玩家栏位无变化, 则不进行移除.
            }
        }

        // 非物品技能直接移除.
        removeAbilityEntity(entity)
    }

    private fun removeAbilityEntity(entity: Entity) {
        entity[CastBy].caster[AbilityContainer].remove(entity[Ability].meta.type, entity)
        entity.remove()
    }
}