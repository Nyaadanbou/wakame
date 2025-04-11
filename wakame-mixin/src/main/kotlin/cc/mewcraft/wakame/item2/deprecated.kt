package cc.mewcraft.wakame.item2

import org.bukkit.entity.LivingEntity
import java.util.*

object ItemDamageEventMarker {
    private val markers: MutableSet<UUID> = mutableSetOf()

    fun markAlreadyDamaged(livingEntity: LivingEntity) {
        markers.add(livingEntity.uniqueId)
    }

    fun isAlreadyDamaged(livingEntity: LivingEntity): Boolean {
        return markers.remove(livingEntity.uniqueId)
    }
}