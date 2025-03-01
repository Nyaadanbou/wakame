package cc.mewcraft.wakame.ecs

import cc.mewcraft.wakame.Injector
import cc.mewcraft.wakame.ecs.component.AbilityComponent
import cc.mewcraft.wakame.ecs.component.BukkitBridgeComponent
import cc.mewcraft.wakame.ecs.component.CastBy
import cc.mewcraft.wakame.ecs.component.ElementComponent
import cc.mewcraft.wakame.ecs.component.IdentifierComponent
import cc.mewcraft.wakame.ecs.component.TargetTo
import com.github.quillraven.fleks.Family
import com.github.quillraven.fleks.FamilyDefinition

object FamilyDefinitions {
    private val wakameWorld: WakameWorld = Injector.get()

    val ABILITY: Family = family { all(AbilityComponent, BukkitBridgeComponent, CastBy, IdentifierComponent) }
    val ELEMENT_STACK: Family = family { all(BukkitBridgeComponent, IdentifierComponent, ElementComponent, TargetTo) }

    private fun family(cfg: FamilyDefinition.() -> Unit): Family {
        return wakameWorld.world().family(cfg)
    }
}