package cc.mewcraft.wakame.entity.attribute

import cc.mewcraft.wakame.entity.typeref.EntityRefLookup
import cc.mewcraft.wakame.registry2.BuiltInRegistries
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player

object AttributeMapFactoryImpl : AttributeMapFactory {

    override fun create(player: Player): AttributeMap {
        val key = EntityType.PLAYER.key // a bit faster than `player.type.key`
        val default = BuiltInRegistries.ATTRIBUTE_SUPPLIER.getOrThrow(key)
        return PlayerAttributeMap(default, player).apply {
            syncToMinecraft() // 同步到世界
        }
    }

    override fun create(entity: Entity): AttributeMap? {
        if (entity is Player)
            return create(entity) // need to redirect to create(Player)
        if (entity !is LivingEntity)
            return null // atm only living entity has attribute map

        val key = EntityRefLookup.get(entity)
        val default = BuiltInRegistries.ATTRIBUTE_SUPPLIER.getOrThrow(key)
        return EntityAttributeMap(default, entity).apply {
            syncToMinecraft() // 同步到世界
        }
    }

}