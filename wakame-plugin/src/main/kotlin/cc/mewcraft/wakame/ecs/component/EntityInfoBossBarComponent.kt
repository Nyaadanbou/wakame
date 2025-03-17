package cc.mewcraft.wakame.ecs.component

import cc.mewcraft.wakame.ecs.bridge.BukkitComponent
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import net.kyori.adventure.bossbar.BossBar

/**
 * 用于存放"提示生物信息的 BossBar" 的组件.
 */
data class EntityInfoBossBarComponent(
    val bossBar: BossBar = BossBar.bossBar(BukkitComponent.empty(), BossBar.MIN_PROGRESS, BossBar.Color.RED, BossBar.Overlay.PROGRESS),
) : Component<EntityInfoBossBarComponent> {
    companion object : ComponentType<EntityInfoBossBarComponent>()

    override fun type(): ComponentType<EntityInfoBossBarComponent> = EntityInfoBossBarComponent
}
