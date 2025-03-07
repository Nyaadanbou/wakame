package cc.mewcraft.wakame.ecs.component

import cc.mewcraft.wakame.ecs.bridge.BukkitEntity
import cc.mewcraft.wakame.ecs.bridge.BukkitPlayer
import cc.mewcraft.wakame.ecs.bridge.FleksEntity
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.github.quillraven.fleks.EntityComponentContext

data class CastBy(
    var caster: FleksEntity,
) : Component<CastBy> {

    context(EntityComponentContext)
    fun player(): BukkitPlayer {
        return caster[BukkitPlayerComponent].bukkitPlayer
    }

    context(EntityComponentContext)
    fun entity(): BukkitEntity {
        return caster[BukkitEntityComponent].bukkitEntity
    }

    context(EntityComponentContext)
    fun entityOrPlayer(): BukkitEntity {
        return if (caster.has(BukkitPlayerComponent)) {
            caster[BukkitPlayerComponent].bukkitPlayer
        } else {
            caster[BukkitEntityComponent].bukkitEntity
        }
    }

    override fun type(): ComponentType<CastBy> = CastBy

    companion object : ComponentType<CastBy>()
}