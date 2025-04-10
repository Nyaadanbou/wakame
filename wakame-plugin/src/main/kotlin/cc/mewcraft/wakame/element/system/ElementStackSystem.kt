package cc.mewcraft.wakame.element.system

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.ability2.AbilityCastUtils
import cc.mewcraft.wakame.ability2.component.CastBy
import cc.mewcraft.wakame.ability2.component.TargetTo
import cc.mewcraft.wakame.ecs.Families
import cc.mewcraft.wakame.ecs.Fleks
import cc.mewcraft.wakame.ecs.bridge.koishify
import cc.mewcraft.wakame.ecs.component.BossBarVisible
import cc.mewcraft.wakame.ecs.component.EntityInfoBossBarComponent
import cc.mewcraft.wakame.ecs.component.TickCountComponent
import cc.mewcraft.wakame.ecs.system.ListenableIteratingSystem
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.element.component.ElementComponent
import cc.mewcraft.wakame.element.component.ElementStackComponent
import cc.mewcraft.wakame.element.component.ElementStackContainer
import cc.mewcraft.wakame.event.bukkit.NekoPostprocessDamageEvent
import cc.mewcraft.wakame.registry2.entry.RegistryEntry
import com.github.quillraven.fleks.Entity
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler

object ElementStackSystem : ListenableIteratingSystem(
    family = Families.ELEMENT_STACK
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
                AbilityCastUtils.castPoint(ability.unwrap(), entity[CastBy].entityOrPlayer(), entity[CastBy].entityOrPlayer())
            }
            elementStackComponent.triggeredLevels.add(requiredAmount)
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun on(event: NekoPostprocessDamageEvent) {
        val damagee = event.damagee
        val damagePackets = event.damageMetadata.damageBundle.packets()
        val causingEntity = event.damageSource.causingEntity
        val target = damagee.koishify().unwrap()
        for (damagePacket in damagePackets) {
            val element = damagePacket.element
            applyElementStack(element, 1, target)
        }

        if (causingEntity != null && causingEntity is Player) {
            causingEntity.koishify().unwrap()[BossBarVisible].bossBar2DurationTick.put(target[EntityInfoBossBarComponent].bossBar, 100)
        }
    }

    private fun removeEntity(entity: Entity, target: Entity) {
        val element = entity[ElementComponent].element
        target[ElementStackContainer].remove(element)
        entity.remove()
        LOGGER.info("在 $entity 上的 ${element.getIdAsString()} 元素效果已失效.")
    }

    /**
     * 对一个目标实体应用元素层数.
     *
     * @param target 目标实体
     * @param element 元素效果的元素
     * @param amount 应用层数
     */
    fun applyElementStack(element: RegistryEntry<Element>, amount: Int, target: Entity) {
        require(amount > 0) { "amount > 0" }
        val stackEffect = element.unwrap().stackEffect
        if (stackEffect == null)
            return
        if (target[ElementStackContainer].contains(element)) {
            addElementStack(target, element, amount)
            return
        }

        target[ElementStackContainer][element] = Fleks.INSTANCE.createEntity {
            it += CastBy(target)
            it += TargetTo(target)
            it += ElementComponent(element)
            it += TickCountComponent(0)
            it += ElementStackComponent(
                effects = stackEffect.stages.associate { it.amount to it.abilities }.toMap(Int2ObjectOpenHashMap()),
                maxAmount = stackEffect.maxAmount,
                disappearTime = stackEffect.disappearTime,
            )
        }
    }

    private fun addElementStack(entity: Entity, element: RegistryEntry<Element>, amount: Int) = with(Fleks.INSTANCE.world) {
        require(amount > 0) { "amount > 0" }
        val stack = entity[ElementStackContainer][element] ?:
        error("ElementStackContainer does not contain element $element")
        stack[ElementStackComponent].amount += amount
        stack[TickCountComponent].tick = 0
    }
}