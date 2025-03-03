package cc.mewcraft.wakame.ecs

import cc.mewcraft.wakame.ecs.component.AbilityComponent
import cc.mewcraft.wakame.ecs.component.BukkitBridgeComponent
import cc.mewcraft.wakame.ecs.component.CastBy
import cc.mewcraft.wakame.ecs.component.ElementComponent
import cc.mewcraft.wakame.ecs.component.IdentifierComponent
import cc.mewcraft.wakame.ecs.component.MechanicComponent
import cc.mewcraft.wakame.ecs.component.TargetTo
import cc.mewcraft.wakame.ecs.component.TickCountComponent
import com.github.quillraven.fleks.Family
import com.github.quillraven.fleks.World.Companion.family

object FamilyDefinitions {
    val ABILITY: Family = family { all(AbilityComponent, CastBy, TargetTo, IdentifierComponent) }
    val ABILITY_BUKKIT_BRIDGE: Family = family { all(AbilityComponent, BukkitBridgeComponent, TargetTo, IdentifierComponent) }
    val ELEMENT_STACK: Family = family { all(IdentifierComponent, ElementComponent, TargetTo) }
    val ELEMENT_STACK_BUKKIT_BRIDGE: Family = family { all(IdentifierComponent, ElementComponent, TargetTo) }
    val MECHANIC: Family = family { all(IdentifierComponent, MechanicComponent, TickCountComponent) }
}