package cc.mewcraft.wakame.ecs.component

import cc.mewcraft.wakame.ecs.bridge.EComponentType
import com.github.quillraven.fleks.Component

import net.kyori.adventure.bossbar.BossBar

data class ManaBossBar(
    val bossBar: BossBar
) : Component<ManaBossBar> {
    companion object : EComponentType<ManaBossBar>()

    override fun type(): EComponentType<ManaBossBar> = ManaBossBar
}
