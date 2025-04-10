package cc.mewcraft.wakame.ecs.component

import cc.mewcraft.wakame.ecs.bridge.EComponent
import cc.mewcraft.wakame.ecs.bridge.EComponentType
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component

/**
 * 用于存放"提示生物信息的 BossBar" 的组件.
 */
data class EntityInfoBossBar(
    val bossBar: BossBar = BossBar.bossBar(Component.empty(), BossBar.MIN_PROGRESS, BossBar.Color.GREEN, BossBar.Overlay.PROGRESS),
) : EComponent<EntityInfoBossBar> {
    companion object : EComponentType<EntityInfoBossBar>()

    override fun type(): EComponentType<EntityInfoBossBar> = EntityInfoBossBar
}
