package cc.mewcraft.wakame.ecs.component

import cc.mewcraft.wakame.ecs.bridge.EComponentType
import com.github.quillraven.fleks.Component

import org.bukkit.entity.Entity

class BukkitEntity(
    delegate: Entity,
) : Component<BukkitEntity>, ObjectWrapper<Entity>(delegate) {
    companion object : EComponentType<BukkitEntity>()

    override fun type(): EComponentType<BukkitEntity> = BukkitEntity
}