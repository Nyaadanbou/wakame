package cc.mewcraft.wakame.attribute

import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player

class AttributeAccessorsImpl : AttributeAccessors {
    @Suppress("UNCHECKED_CAST")
    override fun <T> get(subjectType: Class<T>): AttributeAccessor<T> {
        if (!subjectType.isAssignableFrom(LivingEntity::class.java)) {
            throw IllegalArgumentException("Unsupported subject type: $subjectType")
        }

        return when (subjectType) {
            Player::class.java -> PlayerAttributeAccessor as AttributeAccessor<T>
            else -> EntityAttributeAccessor as AttributeAccessor<T>
        }
    }
}