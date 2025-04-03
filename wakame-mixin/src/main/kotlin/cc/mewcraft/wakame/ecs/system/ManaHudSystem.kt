package cc.mewcraft.wakame.ecs.system

import cc.mewcraft.wakame.ecs.Families
import cc.mewcraft.wakame.ecs.component.BukkitPlayerComponent
import cc.mewcraft.wakame.ecs.component.Mana
import cc.mewcraft.wakame.ecs.component.ManaBossBar
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.FamilyOnAdd
import com.github.quillraven.fleks.Fixed
import com.github.quillraven.fleks.IteratingSystem
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component

class ManaHudSystem : IteratingSystem(
    family = Families.BUKKIT_PLAYER,
    interval = Fixed(5f)
), FamilyOnAdd {

    override fun onTickEntity(entity: Entity) {
        val current = entity[Mana].current
        val maximum = entity[Mana].maximum

        val progress = current.toFloat() / maximum.toFloat()
        val text = Component.text("魔法值 $current / $maximum")

        val bossBar = entity[ManaBossBar].bossBar

        // 更新玩家的 bossBar
        bossBar.name(text)
        bossBar.progress(progress)
    }

    override fun onAddEntity(entity: Entity) {
        entity.configure { it += ManaBossBar(BossBar.bossBar(Component.empty(), 0f, BossBar.Color.BLUE, BossBar.Overlay.PROGRESS)) }
        entity[ManaBossBar].bossBar.addViewer(entity[BukkitPlayerComponent].bukkitPlayer)
    }
}