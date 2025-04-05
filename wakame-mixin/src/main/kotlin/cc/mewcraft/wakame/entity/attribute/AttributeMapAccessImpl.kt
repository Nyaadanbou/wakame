package cc.mewcraft.wakame.entity.attribute

import cc.mewcraft.wakame.ecs.bridge.koishify
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player

/**
 * Provides the access to the [AttributeMap] of a specific subject.
 */
object AttributeMapAccessImpl : AttributeMapAccess {

    override fun get(player: Player): Result<AttributeMap> {
        return Result.success(player.koishify()[AttributeMap])
    }

    override fun get(entity: Entity): Result<AttributeMap> {
        if (entity is Player) {
            return get(entity) // need to redirect to get(Player)
        }

        // TODO #373: 从 ecs 读取
        if (entity !is LivingEntity) {
            return Result.failure(
                IllegalArgumentException("Only LivingEntity has AttributeMap!")
            )
        }

        val value = AttributeMapFactory.INSTANCE.create(entity) ?: return Result.failure(
            IllegalArgumentException("Entity $entity has no AttributeMap!")
        )

        return Result.success(value)
    }

}