package cc.mewcraft.wakame.attribute

import cc.mewcraft.wakame.entity.attribute.AttributeMap
import cc.mewcraft.wakame.entity.attribute.AttributeMapAccess
import cc.mewcraft.wakame.entity.attribute.AttributeMapFactory
import cc.mewcraft.wakame.user.toUser
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player

/**
 * Provides the access to the [cc.mewcraft.wakame.entity.attribute.AttributeMap] of a specific subject.
 */
internal object AttributeMapAccessImpl : AttributeMapAccess {

    override fun get(player: Player): Result<AttributeMap> {
        // TODO #373: 从 ecs 读取
        return Result.success(player.toUser().attributeMap)
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

        return Result.success(AttributeMapFactory.INSTANCE.create(entity))
    }

}