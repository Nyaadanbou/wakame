package cc.mewcraft.wakame.ability.component

import cc.mewcraft.wakame.ecs.bridge.BukkitEntity
import cc.mewcraft.wakame.ecs.bridge.BukkitPlayer
import cc.mewcraft.wakame.ecs.bridge.FleksEntity
import cc.mewcraft.wakame.ecs.component.BukkitEntityComponent
import cc.mewcraft.wakame.ecs.component.BukkitPlayerComponent
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.github.quillraven.fleks.EntityComponentContext

data class CastBy(
    var caster: FleksEntity,
) : Component<CastBy> {
    companion object : ComponentType<CastBy>()

    override fun type(): ComponentType<CastBy> = CastBy

    context(EntityComponentContext)
    fun player(): BukkitPlayer {
        return caster[BukkitPlayerComponent.Companion].bukkitPlayer
    }

    context(EntityComponentContext)
    fun entity(): BukkitEntity {
        return caster[BukkitEntityComponent.Companion].bukkitEntity
    }

    context(EntityComponentContext)
    fun entityOrPlayer(): BukkitEntity {
        return if (caster.has(BukkitPlayerComponent.Companion)) {
            caster[BukkitPlayerComponent.Companion].bukkitPlayer
        } else {
            caster[BukkitEntityComponent.Companion].bukkitEntity
        }
    }
}