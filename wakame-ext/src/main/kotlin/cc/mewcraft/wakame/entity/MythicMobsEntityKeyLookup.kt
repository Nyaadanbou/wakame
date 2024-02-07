package cc.mewcraft.wakame.entity

import cc.mewcraft.wakame.annotation.InternalApi
import net.kyori.adventure.key.Key
import org.bukkit.entity.Entity

@InternalApi
class MythicMobsEntityKeyLookup : EntityKeyLookup {

    private val test: Key = Key.key(MM_NAMESPACE, "test")

    /**
     * Returns `null` if the [entity] is not an MM entity.
     */
    override fun getKey(entity: Entity): Key? {
        // TODO("Add MythicMobs dependency to this module, and implement it")
        return null
    }

    companion object Constants {
        const val MM_NAMESPACE = "mythicmobs"
    }

}