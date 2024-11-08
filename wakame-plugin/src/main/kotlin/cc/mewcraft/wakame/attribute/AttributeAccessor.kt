package cc.mewcraft.wakame.attribute

import cc.mewcraft.wakame.user.UserManager
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

data object AnyAttributeMapAccess : AttributeMapAccess<Any> {
    override fun get(subject: Any): Result<AttributeMap> {
        return runCatching {
            when (subject) {
                is Player -> PlayerAttributeMapAccess.get(subject).getOrThrow()
                is LivingEntity -> EntityAttributeMapAccess.get(subject).getOrThrow()
                else -> throw IllegalArgumentException("Unsupported subject type: ${subject::class.simpleName}")
            }
        }
    }
}

/**
 * Provides the access to the [AttributeMap] of all (online) players.
 */
data object PlayerAttributeMapAccess : KoinComponent, AttributeMapAccess<Player> {
    private val userManager: UserManager<Player> by inject()

    override fun get(subject: Player): Result<AttributeMap> {
        // 该实现仅仅把对函数的调用转发到对应的 User 实例之下
        return runCatching { userManager.getUser(subject).attributeMap }
    }
}

/**
 * Provides the access to [AttributeMap] of all non-player entities.
 */
data object EntityAttributeMapAccess : AttributeMapAccess<LivingEntity> {
    override fun get(subject: LivingEntity): Result<AttributeMap> {
        return runCatching { AttributeMap(subject) }
    }
}