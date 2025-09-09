package cc.mewcraft.wakame.item2.config.property.impl

import net.kyori.adventure.key.Key
import org.bukkit.NamespacedKey
import org.bukkit.entity.EntityType
import org.spongepowered.configurate.objectmapping.ConfigSerializable

/**
 * 使物品可以变成一个可以捕捉绝大部分实体的桶, 类似原版的鱼桶.
 */
@ConfigSerializable
data class EntityBucket(
    val allowedEntities: Set<Key>,
) {
    companion object {
        val SUPPORT_ENTITY_TYPES: Set<NamespacedKey> = listOf(
            EntityType.ALLAY,
            EntityType.ARMADILLO,
            EntityType.BAT,
            EntityType.CAMEL,
            EntityType.CAT,
            EntityType.CHICKEN,
            EntityType.COW,
            //EntityType.DONKEY,
            //EntityType.FROG,
            //EntityType.GLOW_SQUID,
            //EntityType.HAPPY_GHAST,
            //EntityType.HORSE,
            EntityType.MOOSHROOM,
            EntityType.OCELOT,
            //EntityType.PARROT,
            EntityType.PIG,
            EntityType.RABBIT,
            EntityType.SHEEP,
            //EntityType.SKELETON_HORSE,
            //EntityType.SNIFFER,
            EntityType.SNOW_GOLEM,
            //EntityType.SQUID,
            //EntityType.STRIDER,
            EntityType.TURTLE,
            EntityType.VILLAGER,
        ).mapTo(HashSet(32), EntityType::getKey)
    }

    init {
        require(allowedEntities.subtract(SUPPORT_ENTITY_TYPES).isEmpty()) { "unsupported entity types: ${allowedEntities.subtract(SUPPORT_ENTITY_TYPES).joinToString(transform = Key::asString)}" }
    }
}
