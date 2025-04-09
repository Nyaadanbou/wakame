package cc.mewcraft.wakame.hook.impl.mythicmobs.listener

import io.lumine.mythic.bukkit.MythicBukkit
import net.kyori.adventure.key.Key
import org.bukkit.NamespacedKey
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.persistence.PersistentDataType

object CreatureSpawnListener : Listener {
    private val PDC_KEY = NamespacedKey("koish", "mob_id")

    private val MYTHIC_API: MythicBukkit by lazy { MythicBukkit.inst() }

    @EventHandler
    fun on(e: CreatureSpawnEvent) {
        val mobId = e.entity.persistentDataContainer.get(PDC_KEY, PersistentDataType.STRING)?.let { Key.key(it) } ?: return
        val mob = MYTHIC_API.apiHelper.getMythicMob(mobId.value())
    }
}