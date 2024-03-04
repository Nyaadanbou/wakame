package cc.mewcraft.wakame.resource

import cc.mewcraft.wakame.attribute.PlayerAttributeAccessor
import cc.mewcraft.wakame.attribute.PlayerAttributeMap
import org.bukkit.Bukkit
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.*

/**
 * Provides the access to [ResourceMap] of **all** players.
 *
 * Not thread-safe.
 */
class ResourceAccessor : KoinComponent {
    private val playerAttributeAccessor: PlayerAttributeAccessor by inject()
    private val cacheMap: MutableMap<UUID, ResourceMap> = HashMap()

    fun getResourceMap(uuid: UUID): ResourceMap {
        return cacheMap.computeIfAbsent(uuid) {
            val attributeMap = playerAttributeAccessor.getAttributeMap(Bukkit.getPlayer(it)!!)
            val resourceSupplier = ResourceSupplier(attributeMap as PlayerAttributeMap)
            ResourceMap(resourceSupplier)
        }
    }

    fun removeResourceMap(uuid: UUID) {
        cacheMap.remove(uuid)
    }
}