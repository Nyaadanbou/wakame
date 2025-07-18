package cc.mewcraft.wakame.hook.impl.towny

import cc.mewcraft.wakame.util.metadata.AbstractMetadataRegistry
import cc.mewcraft.wakame.util.metadata.MetadataKey
import cc.mewcraft.wakame.util.metadata.MetadataMap
import com.google.common.collect.ImmutableMap
import com.palmergames.bukkit.towny.`object`.Town
import java.util.*

object TownMetadataRegistry : AbstractMetadataRegistry<UUID>() {
    fun provide(town: Town): MetadataMap {
        return provide(town.uuid)
    }

    fun get(town: Town): Optional<MetadataMap> {
        return get(town.uuid)
    }

    fun <K : Any> getAllWithKey(key: MetadataKey<K>): MutableMap<Town, K> {
        val ret = ImmutableMap.builder<Town, K>()
        this.cache.asMap().forEach { uuid, map ->
            map.get(key).ifPresent { t: K ->
                val town = TownyHook.TOWNY.getTown(uuid)
                if (town != null) {
                    ret.put(town, t)
                }
            }
        }
        return ret.build()
    }
}