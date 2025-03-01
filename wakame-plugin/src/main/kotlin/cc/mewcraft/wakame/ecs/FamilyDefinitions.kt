package cc.mewcraft.wakame.ecs

import cc.mewcraft.wakame.ecs.component.AbilityComponent
import cc.mewcraft.wakame.ecs.component.BukkitBridgeComponent
import cc.mewcraft.wakame.ecs.component.CastBy
import cc.mewcraft.wakame.ecs.component.ElementComponent
import cc.mewcraft.wakame.ecs.component.IdentifierComponent
import cc.mewcraft.wakame.ecs.component.MechanicComponent
import cc.mewcraft.wakame.ecs.component.TargetTo
import com.github.quillraven.fleks.Family
import com.github.quillraven.fleks.World.Companion.family

object FamilyDefinitions {
    val ABILITY: Family = family { all(AbilityComponent, BukkitBridgeComponent, CastBy, IdentifierComponent) }
    val ELEMENT_STACK: Family = family { all(BukkitBridgeComponent, IdentifierComponent, ElementComponent, TargetTo) }
    val MECHANIC: Family = family { all(IdentifierComponent, MechanicComponent) }
}