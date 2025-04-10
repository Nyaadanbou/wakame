package cc.mewcraft.wakame.ability2.component

import cc.mewcraft.wakame.ecs.bridge.EComponentType
import cc.mewcraft.wakame.ecs.bridge.EEntity
import cc.mewcraft.wakame.ecs.component.BukkitEntity
import cc.mewcraft.wakame.ecs.component.BukkitPlayer
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.EntityComponentContext
import org.bukkit.entity.Entity
import org.bukkit.entity.Player

data class TargetTo(
    var target: EEntity,
) : Component<TargetTo> {
    companion object : EComponentType<TargetTo>()

    override fun type(): EComponentType<TargetTo> = TargetTo


    context(EntityComponentContext)
    fun player(): Player {
        return target[BukkitPlayer].unwrap()
    }

    context(EntityComponentContext)
    fun entity(): Entity {
        return target[BukkitEntity].unwrap()
    }

    context(EntityComponentContext)
    fun entityOrPlayer(): Entity {
        return if (target.has(BukkitPlayer)) {
            target[BukkitPlayer].unwrap()
        } else {
            target[BukkitEntity].unwrap()
        }
    }
}