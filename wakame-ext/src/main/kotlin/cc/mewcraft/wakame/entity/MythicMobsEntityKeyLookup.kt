package cc.mewcraft.wakame.entity

import cc.mewcraft.wakame.util.Key
import io.lumine.mythic.bukkit.MythicBukkit
import net.kyori.adventure.key.Key
import org.bukkit.entity.Entity
import kotlin.jvm.optionals.getOrElse

class MythicMobsEntityKeyLookup : EntityKeyLookupPart {

    private val mythicApi: MythicBukkit by lazy { MythicBukkit.inst() }

    /**
     * Returns `null` if the [entity] is not an MM entity.
     */
    override fun get(entity: Entity): Key? {
        val activeMob = mythicApi.mobManager.getActiveMob(entity.uniqueId).getOrElse {
            return null
        }
        return Key(EntitySupport.NAMESPACE_MYTHIC_MOBS, activeMob.name)
    }

    override fun validate(key: Key): Boolean {
        if (key.namespace() != EntitySupport.NAMESPACE_MYTHIC_MOBS) {
            return false
        }
        return mythicApi.mobManager.mobNames.contains(key.value())
    }

}

private object EntitySupport {
    const val NAMESPACE_MYTHIC_MOBS = "mythicmobs"
}