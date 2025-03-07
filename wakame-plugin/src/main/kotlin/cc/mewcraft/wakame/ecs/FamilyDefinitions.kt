package cc.mewcraft.wakame.ecs

import cc.mewcraft.wakame.ecs.component.AbilityComponent
import cc.mewcraft.wakame.ecs.component.BukkitBlockComponent
import cc.mewcraft.wakame.ecs.component.BukkitEntityComponent
import cc.mewcraft.wakame.ecs.component.BukkitObject
import cc.mewcraft.wakame.ecs.component.CastBy
import cc.mewcraft.wakame.ecs.component.ElementComponent
import cc.mewcraft.wakame.ecs.component.IdentifierComponent
import cc.mewcraft.wakame.ecs.component.BukkitPlayerComponent
import cc.mewcraft.wakame.ecs.component.TargetTo
import cc.mewcraft.wakame.ecs.component.TickCountComponent
import cc.mewcraft.wakame.ecs.component.WithAbility
import com.github.quillraven.fleks.Family
import com.github.quillraven.fleks.World.Companion.family

object FamilyDefinitions {
    val BUKKIT_BLOCK: Family = family { all(BukkitObject, BukkitBlockComponent) }
    val BUKKIT_ENTITY: Family = family { all(BukkitObject, BukkitEntityComponent) }
    val BUKKIT_PLAYER: Family = family { all(BukkitObject, BukkitPlayerComponent, BukkitEntityComponent, WithAbility) }
    val ABILITY: Family = family { all(AbilityComponent, CastBy, TargetTo, TickCountComponent, IdentifierComponent) }
    val ELEMENT_STACK: Family = family { all(IdentifierComponent, ElementComponent, TargetTo) }
}