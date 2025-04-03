package cc.mewcraft.wakame.ecs.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import net.kyori.adventure.bossbar.BossBar

data class ManaBossBar(
    val bossBar: BossBar
) : Component<ManaBossBar> {
    companion object : ComponentType<ManaBossBar>()

    override fun type(): ComponentType<ManaBossBar> = ManaBossBar
}
