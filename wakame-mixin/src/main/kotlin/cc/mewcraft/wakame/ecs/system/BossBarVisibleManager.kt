package cc.mewcraft.wakame.ecs.system

import cc.mewcraft.wakame.ecs.Families
import cc.mewcraft.wakame.ecs.component.BossBarVisible
import cc.mewcraft.wakame.ecs.component.BukkitPlayer
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.FamilyOnAdd
import com.github.quillraven.fleks.IteratingSystem
import org.bukkit.entity.Player

class BossBarVisibleManager : IteratingSystem(
    family = Families.BUKKIT_PLAYER
), FamilyOnAdd {

    override fun onTickEntity(entity: Entity) {
        val player = entity[BukkitPlayer].unwrap()
        val bossBarVisible = entity[BossBarVisible]
        bossBarVisible.tickAndRemoveExpiredBossBars(player)
        if (bossBarVisible.bossBar2DurationTick.isEmpty()) return

        bossBarVisible.bossBar2DurationTick.forEach { (bossBar, _) ->
            bossBar.addViewer(player)
        }
    }

    override fun onAddEntity(entity: Entity) {
        entity.configure { it += BossBarVisible() }
    }

    private fun BossBarVisible.tickAndRemoveExpiredBossBars(player: Player) {
        val iterator = bossBar2DurationTick.iterator()
        while (iterator.hasNext()) {
            val (bossBar, durationTick) = iterator.next()
            val newTicks = durationTick - 1

            if (durationTick <= 0) {
                bossBar.removeViewer(player)
                iterator.remove()
            } else {
                bossBar2DurationTick.replace(bossBar, newTicks)
            }
        }
    }
}