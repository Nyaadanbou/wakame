package cc.mewcraft.wakame.element.effect

import cc.mewcraft.wakame.ability.character.Caster
import cc.mewcraft.wakame.ability.character.Target
import cc.mewcraft.wakame.ecs.WakameWorld
import cc.mewcraft.wakame.ecs.component.CastBy
import cc.mewcraft.wakame.ecs.component.ElementComponent
import cc.mewcraft.wakame.ecs.component.EntityType
import cc.mewcraft.wakame.ecs.component.StackCountComponent
import cc.mewcraft.wakame.ecs.component.TargetTo
import cc.mewcraft.wakame.ecs.component.TickCountComponent
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.registry2.entry.RegistryEntry
import org.bukkit.entity.LivingEntity
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

internal inline fun <T> elementEffectWorldInteraction(block: ElementEffectWorldInteraction.() -> T): T {
    return ElementEffectWorldInteraction.block()
}

@DslMarker
internal annotation class ElementEffectWorldInteractionDsl

@ElementEffectWorldInteractionDsl
object ElementEffectWorldInteraction : KoinComponent {
    private val wakameWorld: WakameWorld by inject()

    fun ElementEffect.putIntoWorld(caster: Caster?, target: Target) {
        wakameWorld.createEntity(element.getIdAsString()) {
            it += EntityType.ELEMENT_EFFECT
            caster?.let { c -> it += CastBy(c) }
            it += TargetTo(target)
            it += ElementComponent(element)
            it += TickCountComponent()
            it += StackCountComponent()
        }
    }

    fun LivingEntity.containsElementEffect(element: RegistryEntry<out Element>): Boolean {
        var contains = false
        with(wakameWorld.world()) {
            forEach { entity ->
                val family = family { all(EntityType.ELEMENT_EFFECT, ElementComponent, TargetTo) }
                if (!family.contains(entity))
                    return@forEach
                if (entity[ElementComponent].element != element)
                    return@forEach
                if (entity[TargetTo].target.bukkitEntity != this@containsElementEffect)
                    return@forEach
                contains = true
            }
        }
        return contains
    }

    fun LivingEntity.addElementEffect(elementEffect: ElementEffect) {
        with(wakameWorld.world()) {
            forEach { entity ->
                val family = family { all(EntityType.ELEMENT_EFFECT, StackCountComponent, TargetTo, ElementComponent, TickCountComponent) }
                if (!family.contains(entity))
                    return@forEach
                if (entity[ElementComponent].element != elementEffect.element)
                    return@forEach
                if (entity[TargetTo].target.bukkitEntity != this@addElementEffect)
                    return@forEach
                entity[StackCountComponent].count++
                entity[TickCountComponent].tick = .0
                return@forEach
            }
        }
    }
}