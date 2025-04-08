package cc.mewcraft.wakame.hook.impl.mythicmobs

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.entity.typeref.EntityRefLookup
import io.lumine.mythic.bukkit.MythicBukkit
import net.kyori.adventure.key.InvalidKeyException
import net.kyori.adventure.key.Key
import org.bukkit.entity.Entity
import kotlin.jvm.optionals.getOrElse

class MythicMobsEntityRefLookupDictionary : EntityRefLookup.Dictionary {

    private val mythicApi: MythicBukkit by lazy { MythicBukkit.inst() }

    /**
     * Returns `null` if the [entity] is not an MM entity.
     */
    override fun get(entity: Entity): Key? {
        val activeMob = mythicApi.mobManager.getActiveMob(entity.uniqueId).getOrElse {
            return null
        }
        return try {
            Key.key(NAMESPACE, activeMob.name)
        } catch (e: InvalidKeyException) {
            LOGGER.error("Entity belongs to MythicMobs but its name format (${activeMob.name}) is invalid")
            null
        }
    }

    override fun validate(key: Key): Boolean {
        if (key.namespace() != NAMESPACE) {
            return false
        }
        return mythicApi.mobManager.mobNames.contains(key.value())
    }

    companion object Constants {
        private const val NAMESPACE = "mythicmobs"
    }
}
