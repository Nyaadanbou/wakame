package cc.mewcraft.wakame.ability.component

import cc.mewcraft.wakame.ecs.bridge.BukkitEntity
import cc.mewcraft.wakame.ecs.bridge.BukkitPlayer
import cc.mewcraft.wakame.ecs.bridge.FleksEntity
import cc.mewcraft.wakame.ecs.component.BukkitEntityComponent
import cc.mewcraft.wakame.ecs.component.BukkitPlayerComponent
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.github.quillraven.fleks.EntityComponentContext

data class TargetTo(
    var target: FleksEntity
) : Component<TargetTo> {
    companion object : ComponentType<TargetTo>()

    override fun type(): ComponentType<TargetTo> = TargetTo


    context(EntityComponentContext)
    fun player(): BukkitPlayer {
        return target[BukkitPlayerComponent.Companion].bukkitPlayer
    }

    context(EntityComponentContext)
    fun entity(): BukkitEntity {
        return target[BukkitEntityComponent.Companion].bukkitEntity
    }

    context(EntityComponentContext)
    fun entityOrPlayer(): BukkitEntity {
        return if (target.has(BukkitPlayerComponent.Companion)) {
            target[BukkitPlayerComponent.Companion].bukkitPlayer
        } else {
            target[BukkitEntityComponent.Companion].bukkitEntity
        }
    }
}