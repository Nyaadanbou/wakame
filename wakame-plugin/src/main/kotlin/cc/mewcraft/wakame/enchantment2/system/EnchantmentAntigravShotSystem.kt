package cc.mewcraft.wakame.enchantment2.system

import cc.mewcraft.wakame.ecs.bridge.koishify
import cc.mewcraft.wakame.ecs.component.BukkitObject
import cc.mewcraft.wakame.ecs.component.BukkitPlayerComponent
import cc.mewcraft.wakame.ecs.system.ListenableIteratingSystem
import cc.mewcraft.wakame.enchantment2.component.AntigravShot
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.EntityShootBowEvent

object EnchantmentAntigravShotSystem : ListenableIteratingSystem(
    family = World.family { all(BukkitObject, BukkitPlayerComponent, AntigravShot) }
) {

    override fun onTickEntity(entity: Entity) {
        // 无操作
    }

    @EventHandler
    fun on(event: EntityShootBowEvent) {
        val player = event.entity as? Player ?: return
        val playerEntity = player.koishify().unwrap()
        if (!playerEntity.has(AntigravShot)) return

        val projectile = event.projectile
        projectile.setGravity(false) // Paper 可以单独设置箭矢的存活时间, 所以不用担心玩家对天射箭然后导致箭矢长期驻留内存
    }

}