package cc.mewcraft.wakame.entity

import cc.mewcraft.wakame.annotation.InternalApi
import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.LoadingCache
import io.lumine.mythic.bukkit.MythicBukkit
import net.kyori.adventure.key.Key
import org.bukkit.entity.Entity
import kotlin.jvm.optionals.getOrElse

@InternalApi
class MythicMobsEntityKeyLookup : EntityKeyLookup {

    private val mythicApi: MythicBukkit by lazy { MythicBukkit.inst() }
    private val keyCache: LoadingCache<String, Key> = Caffeine.newBuilder()
        .maximumSize(128)
        .build { name ->
            Key.key(MM_NAMESPACE, name)
        }

    /**
     * Returns `null` if the [entity] is not an MM entity.
     */
    override fun getKey(entity: Entity): Key? {
        val activeMob = mythicApi.mobManager.getActiveMob(entity.uniqueId).getOrElse {
            return null
        }

        return keyCache[activeMob.name]
    }

    companion object Constants {
        const val MM_NAMESPACE = "mythicmobs"
    }

}