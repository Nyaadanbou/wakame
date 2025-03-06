package cc.mewcraft.wakame.ecs

import cc.mewcraft.wakame.ecs.component.AbilityComponent
import cc.mewcraft.wakame.ecs.component.BlockComponent
import cc.mewcraft.wakame.ecs.component.BukkitEntityComponent
import cc.mewcraft.wakame.ecs.component.CastBy
import cc.mewcraft.wakame.ecs.component.ElementComponent
import cc.mewcraft.wakame.ecs.component.IdentifierComponent
import cc.mewcraft.wakame.ecs.component.PlayerComponent
import cc.mewcraft.wakame.ecs.component.TargetTo
import cc.mewcraft.wakame.ecs.component.TickCountComponent
import cc.mewcraft.wakame.ecs.component.WithAbility
import com.github.quillraven.fleks.Family
import com.github.quillraven.fleks.World.Companion.family

object FamilyDefinitions {
    val ABILITY: Family = family { all(AbilityComponent, CastBy, TargetTo, TickCountComponent, IdentifierComponent) }
    val BLOCK: Family = family { all(BlockComponent) }
    val BUKKIT_ENTITY: Family = family { all(BukkitEntityComponent) }
    val ELEMENT_STACK: Family = family { all(IdentifierComponent, ElementComponent, TargetTo) }
    val PLAYER: Family = family { all(PlayerComponent, BukkitEntityComponent, WithAbility) }
}