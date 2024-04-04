package cc.mewcraft.wakame.util

import cc.mewcraft.wakame.shadow.world.ShadowCraftWorld0
import me.lucko.shadow.bukkit.BukkitShadowFactory
import me.lucko.shadow.shadow
import org.bukkit.Bukkit
import org.bukkit.craftbukkit.v1_20_R3.CraftServer
import org.bukkit.entity.Entity
import java.util.UUID

object NmsEntityUtils {
    fun getEntity(entityId: Int): Entity? {
        val server = Bukkit.getServer() as CraftServer
        for (world in server.worlds) {
            BukkitShadowFactory.global()
                .shadow<ShadowCraftWorld0>(world)
                .getWorld()
                .entityLookup
                .get(entityId)
                ?.let { return it.bukkitEntity }
        }
        return null
    }

    fun getEntity(entityUniqueId: UUID): Entity? {
        val server = Bukkit.getServer() as CraftServer
        for (world in server.worlds) {
            BukkitShadowFactory.global()
                .shadow<ShadowCraftWorld0>(world)
                .getWorld()
                .entityLookup
                .get(entityUniqueId)
                ?.let { return it.bukkitEntity }
        }
        return null
    }
}