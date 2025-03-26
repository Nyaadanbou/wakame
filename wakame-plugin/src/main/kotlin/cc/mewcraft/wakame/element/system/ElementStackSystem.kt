package cc.mewcraft.wakame.element.system

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.ability2.AbilityCastUtils
import cc.mewcraft.wakame.ability2.component.CastBy
import cc.mewcraft.wakame.ability2.component.TargetTo
import cc.mewcraft.wakame.ecs.KoishFamilies
import cc.mewcraft.wakame.ecs.bridge.koishify
import cc.mewcraft.wakame.ecs.component.BossBarVisible
import cc.mewcraft.wakame.ecs.component.EntityInfoBossBarComponent
import cc.mewcraft.wakame.ecs.component.TickCountComponent
import cc.mewcraft.wakame.ecs.system.ListenableIteratingSystem
import cc.mewcraft.wakame.element.ElementStackManager
import cc.mewcraft.wakame.element.component.ElementComponent
import cc.mewcraft.wakame.element.component.ElementStackComponent
import cc.mewcraft.wakame.element.component.ElementStackContainer
import cc.mewcraft.wakame.event.bukkit.NekoEntityDamageEvent
import com.github.quillraven.fleks.Entity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler

class ElementStackSystem : ListenableIteratingSystem(
    family = KoishFamilies.ELEMENT_STACK
) {
    override fun onTickEntity(entity: Entity) {
        val caster = entity[CastBy].caster
        val target = entity[TargetTo].target
        if (target !in world || caster !in world) {
            entity.remove()
            return
        }

        val tickCountComponent = entity[TickCountComponent]
        val elementStackComponent = entity[ElementStackComponent]

        // 如果 stackCount 小于等于 0，移除实体
        if (elementStackComponent.amount <= 0) {
            removeEntity(entity, target)
            return
        }

        // 如果 tickCount 达到设置好的时间, 移除异常效果.
        if (tickCountComponent.tick >= elementStackComponent.disappearTime) {
            removeEntity(entity, target)
        }

        val effects = elementStackComponent.effects.int2ObjectEntrySet().iterator()
        while (effects.hasNext()) {
            val (requiredAmount, abilities) = effects.next()
            if (elementStackComponent.triggeredLevels.contains(requiredAmount))
                continue
            if (elementStackComponent.amount < requiredAmount)
                continue
            for (ability in abilities) {
                AbilityCastUtils.castPoint(ability.value, entity[CastBy].entityOrPlayer(), entity[CastBy].entityOrPlayer())
            }
            elementStackComponent.triggeredLevels.add(requiredAmount)
        }
    }

    @EventHandler(ignoreCancelled = true)
    private fun onNekoDamage(event: NekoEntityDamageEvent) {
        val damagee = event.damagee
        val damagePackets = event.damageMetadata.damageBundle.packets()
        val causingEntity = event.damageSource.causingEntity

        val target = if (damagee is Player) {
            damagee.koishify()
        } else {
            damagee.koishify()
        }

        for (damagePacket in damagePackets) {
            val element = damagePacket.element

            if (damagee is Player) {
                ElementStackManager.applyElementStack(element, 1, target)
            } else {
                ElementStackManager.applyElementStack(element, 1, target)
            }
        }

        if (causingEntity != null && causingEntity is Player) {
            causingEntity.koishify()[BossBarVisible].bossBar2DurationTick.put(target[EntityInfoBossBarComponent].bossBar, 100)
        }
    }

    private fun removeEntity(entity: Entity, target: Entity) {
        val element = entity[ElementComponent].element
        target[ElementStackContainer].remove(element)
        entity.remove()
        LOGGER.info("在 $entity 上的 ${element.getIdAsString()} 元素效果已失效.")
    }
}