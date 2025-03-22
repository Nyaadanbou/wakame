package cc.mewcraft.wakame.ecs.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import net.kyori.adventure.bossbar.BossBar

data class BossBarVisible(
    val bossBar2DurationTick: Object2IntOpenHashMap<BossBar> = Object2IntOpenHashMap(),
) : Component<BossBarVisible> {
    companion object : ComponentType<BossBarVisible>()

    override fun type(): ComponentType<BossBarVisible> = BossBarVisible
}
