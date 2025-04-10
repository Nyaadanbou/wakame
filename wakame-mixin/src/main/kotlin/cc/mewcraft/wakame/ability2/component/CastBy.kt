package cc.mewcraft.wakame.ability2.component

import cc.mewcraft.wakame.ecs.bridge.EComponentType
import cc.mewcraft.wakame.ecs.bridge.EEntity
import cc.mewcraft.wakame.ecs.component.BukkitEntity
import cc.mewcraft.wakame.ecs.component.BukkitPlayer
import com.github.quillraven.fleks.Component

import com.github.quillraven.fleks.EntityComponentContext
import org.bukkit.entity.Entity
import org.bukkit.entity.Player

data class CastBy(
    var caster: EEntity,
) : Component<CastBy> {
    companion object : EComponentType<CastBy>()

    override fun type(): EComponentType<CastBy> = CastBy

    context(EntityComponentContext)
    fun player(): Player {
        return caster[BukkitPlayer].unwrap()
    }

    context(EntityComponentContext)
    fun entity(): Entity {
        return caster[BukkitEntity].unwrap()
    }

    context(EntityComponentContext)
    fun entityOrPlayer(): Entity {
        return if (caster.has(BukkitPlayer)) {
            caster[BukkitPlayer].unwrap()
        } else {
            caster[BukkitEntity].unwrap()
        }
    }
}