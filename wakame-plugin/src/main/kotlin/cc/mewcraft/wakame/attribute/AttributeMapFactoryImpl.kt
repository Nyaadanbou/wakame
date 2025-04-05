package cc.mewcraft.wakame.attribute

import cc.mewcraft.wakame.Injector
import cc.mewcraft.wakame.entity.attribute.AttributeMap
import cc.mewcraft.wakame.entity.attribute.AttributeMapFactory
import cc.mewcraft.wakame.entity.typeref.EntityRefLookup
import cc.mewcraft.wakame.registry2.KoishRegistries
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player

internal object AttributeMapFactoryImpl : AttributeMapFactory {

    private val entityRefLookup: EntityRefLookup by Injector.inject()

    override fun create(player: Player): AttributeMap {
        val key = EntityType.PLAYER.key // a bit faster than `player.type.key`
        val default = KoishRegistries.ATTRIBUTE_SUPPLIER.getOrThrow(key)
        return PlayerAttributeMap(default, player).apply {
            syncToMinecraft() // apply side effects
        }
    }

    override fun create(entity: LivingEntity): AttributeMap {
        val key = entityRefLookup.get(entity)
        val default = KoishRegistries.ATTRIBUTE_SUPPLIER.getOrThrow(key)
        return EntityAttributeMap(default, entity).apply {
            syncToMinecraft() // apply side effects
        }
    }

}