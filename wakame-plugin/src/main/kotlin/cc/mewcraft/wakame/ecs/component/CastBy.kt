package cc.mewcraft.wakame.ecs.component

import cc.mewcraft.wakame.ecs.bridge.BukkitEntity
import cc.mewcraft.wakame.ecs.bridge.BukkitPlayer
import cc.mewcraft.wakame.ecs.external.KoishEntity
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

data class CastBy(
    var caster: KoishEntity
) : Component<CastBy> {

    fun player(): BukkitPlayer {
        return caster[PlayerComponent].player
    }

    fun entity(): BukkitEntity {
        return caster[BukkitEntityComponent].bukkitEntity
    }

    fun entityOrPlayer(): BukkitEntity {
        return if (caster.has(PlayerComponent)) {
            caster[PlayerComponent].player
        } else {
            caster[BukkitEntityComponent].bukkitEntity
        }
    }

    override fun type(): ComponentType<CastBy> = CastBy

    companion object : ComponentType<CastBy>()
}