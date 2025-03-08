package cc.mewcraft.wakame.elementstack

import cc.mewcraft.wakame.ecs.Fleks
import cc.mewcraft.wakame.ecs.Families
import cc.mewcraft.wakame.ecs.component.BukkitEntityComponent
import cc.mewcraft.wakame.ecs.component.CastBy
import cc.mewcraft.wakame.ecs.component.ElementComponent
import cc.mewcraft.wakame.ecs.component.IdentifierComponent
import cc.mewcraft.wakame.ecs.component.StackCountComponent
import cc.mewcraft.wakame.ecs.component.TargetTo
import cc.mewcraft.wakame.ecs.component.TickCountComponent
import cc.mewcraft.wakame.ecs.bridge.KoishEntity
import cc.mewcraft.wakame.element.ElementType
import cc.mewcraft.wakame.registry2.entry.RegistryEntry
import org.bukkit.entity.LivingEntity

internal inline fun <T> elementStackWorldInteraction(block: ElementStackWorldInteraction.() -> T): T {
    return ElementStackWorldInteraction.block()
}

@DslMarker
internal annotation class ElementStackWorldInteractionDsl

@ElementStackWorldInteractionDsl
object ElementStackWorldInteraction {

    fun putElementStackIntoWorld(element: RegistryEntry<ElementType>, count: Int, target: KoishEntity, caster: KoishEntity?) {
        require(count > 0) { "Count must be greater than 0" }
        Fleks.createEntity {
            it += IdentifierComponent(element.getKeyOrThrow().value)
            caster?.let { c -> it += CastBy(caster.entity) }
            it += TargetTo(target.entity)
            it += ElementComponent(element)
            it += TickCountComponent()
            it += StackCountComponent(count)
        }
    }

    fun LivingEntity.containsElementStack(element: RegistryEntry<ElementType>): Boolean {
        var contains = false
        Families.ELEMENT_STACK.forEach { entity ->
            if (entity[ElementComponent].element != element)
                return@forEach
            if (entity[TargetTo].target[BukkitEntityComponent].bukkitEntity != this@containsElementStack)
                return@forEach
            contains = true
        }
        return contains
    }

    fun LivingEntity.addElementStack(element: RegistryEntry<ElementType>, count: Int) {
        Families.ELEMENT_STACK.forEach { entity ->
            if (entity[ElementComponent].element != element)
                return@forEach
            if (entity[TargetTo].target[BukkitEntityComponent].bukkitEntity != this@addElementStack)
                return@forEach
            entity[StackCountComponent].count += count
            entity[TickCountComponent].tick = .0
            return@forEach
        }
    }
}