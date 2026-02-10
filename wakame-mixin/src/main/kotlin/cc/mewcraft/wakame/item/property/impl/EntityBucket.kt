package cc.mewcraft.wakame.item.property.impl

import net.kyori.adventure.key.Key
import org.bukkit.NamespacedKey
import org.bukkit.entity.EntityType
import org.spongepowered.configurate.objectmapping.ConfigSerializable

/**
 * 使物品可以变成一个可以捕捉绝大部分实体的桶, 类似原版的鱼桶.
 */
@ConfigSerializable
data class EntityBucket(
    val itemNameFormat: String = "<entity_type>桶",
    val canCaptureBabies: Boolean = false,
    val allowedEntityTypes: Set<Key> = emptySet(),
    val consumeOnRelease: Boolean = true,
) {
    companion object {
        val SUPPORT_ENTITY_TYPES: Set<NamespacedKey> = listOf(
            // Animals
            EntityType.ARMADILLO,
            EntityType.BEE,
            EntityType.CAMEL,
            EntityType.CAT,
            EntityType.CHICKEN,
            EntityType.COW,
            EntityType.DOLPHIN,
            EntityType.DONKEY,
            EntityType.FOX,
            EntityType.FROG,
            EntityType.GLOW_SQUID,
            EntityType.GOAT,
            EntityType.HAPPY_GHAST,
            EntityType.HOGLIN,
            EntityType.HORSE,
            EntityType.LLAMA,
            EntityType.MOOSHROOM,
            EntityType.MULE,
            EntityType.OCELOT,
            EntityType.PANDA,
            EntityType.PARROT,
            EntityType.PIG,
            EntityType.POLAR_BEAR,
            EntityType.RABBIT,
            EntityType.SHEEP,
            EntityType.SKELETON_HORSE,
            EntityType.SNIFFER,
            EntityType.SQUID,
            EntityType.STRIDER,
            EntityType.TRADER_LLAMA,
            EntityType.TURTLE,
            EntityType.WOLF,

            // Animals Like
            EntityType.ALLAY,
            EntityType.IRON_GOLEM,
            EntityType.SNOW_GOLEM,

            // NPCs
            EntityType.VILLAGER,
            EntityType.WANDERING_TRADER,
            EntityType.ZOMBIE_VILLAGER,
        ).mapTo(HashSet(32), EntityType::getKey)
    }

    init {
        require(allowedEntityTypes.subtract(SUPPORT_ENTITY_TYPES).isEmpty()) { "unsupported entity types: ${allowedEntityTypes.subtract(SUPPORT_ENTITY_TYPES).joinToString(transform = Key::asString)}" }
    }
}
