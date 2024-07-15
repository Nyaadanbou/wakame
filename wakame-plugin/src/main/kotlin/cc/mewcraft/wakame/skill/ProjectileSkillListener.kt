package cc.mewcraft.wakame.skill

import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent
import com.google.common.collect.Multimap
import com.google.common.collect.MultimapBuilder
import me.lucko.helper.event.Subscription
import org.bukkit.entity.Projectile
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import java.util.UUID

class ProjectileSkillListener : Listener {

    /**
     * K - 弹射物实体的 UUID.
     * V - 该弹射物的事件订阅.
     */
    private val projectileSubscriptions: Multimap<UUID, Subscription> = MultimapBuilder.hashKeys()
        .arrayListValues()
        .build()

    fun addSubscription(projectileUUID: UUID, subscription: Subscription) {
        projectileSubscriptions.put(projectileUUID, subscription)
    }

    private fun removeSubscriptions(projectileUUID: UUID) {
        projectileSubscriptions.removeAll(projectileUUID).forEach { it.unregister() }
    }

    @EventHandler
    private fun onEntityRemove(event: EntityRemoveFromWorldEvent) {
        // TODO: 如果实体从别的世界生成, uuid会不会变?
        val entity = event.entity
        if (entity !is Projectile) return
        removeSubscriptions(entity.uniqueId)
    }
}