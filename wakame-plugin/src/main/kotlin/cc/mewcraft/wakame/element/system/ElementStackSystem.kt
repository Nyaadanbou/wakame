package cc.mewcraft.wakame.element.system

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.ability.component.CastBy
import cc.mewcraft.wakame.ability.component.TargetTo
import cc.mewcraft.wakame.ability.context.abilityInput
import cc.mewcraft.wakame.ecs.Families
import cc.mewcraft.wakame.ecs.bridge.koishify
import cc.mewcraft.wakame.ecs.component.BossBarVisible
import cc.mewcraft.wakame.ecs.component.EntityInfoBossBarComponent
import cc.mewcraft.wakame.ecs.component.TickCountComponent
import cc.mewcraft.wakame.element.ElementStackManager
import cc.mewcraft.wakame.element.component.ElementComponent
import cc.mewcraft.wakame.element.component.ElementStackComponent
import cc.mewcraft.wakame.element.component.ElementStackContainer
import cc.mewcraft.wakame.event.bukkit.NekoEntityDamageEvent
import cc.mewcraft.wakame.util.KoishListener
import cc.mewcraft.wakame.util.event
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import org.bukkit.entity.Player

class ElementStackSystem : IteratingSystem(
    family = Families.ELEMENT_STACK
) {
    private lateinit var damageListener: KoishListener

    override fun onInit() {
        damageListener = event<NekoEntityDamageEvent> { event ->
            if (event.isCancelled)
                return@event
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
    }

    override fun onDispose() {
        damageListener.unregister()
    }

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
        val abilityInput = abilityInput(caster, target)
        while (effects.hasNext()) {
            val (requiredAmount, abilities) = effects.next()
            if (elementStackComponent.triggeredLevels.contains(requiredAmount))
                continue
            if (elementStackComponent.amount < requiredAmount)
                continue
            for (ability in abilities) {
                ability.value.cast(abilityInput)
            }
            elementStackComponent.triggeredLevels.add(requiredAmount)
        }
    }

    private fun removeEntity(entity: Entity, target: Entity) {
        val element = entity[ElementComponent].element
        target[ElementStackContainer].remove(element)
        entity.remove()
        LOGGER.info("在 $entity 上的 ${element.getIdAsString()} 元素效果已失效.")
    }
}