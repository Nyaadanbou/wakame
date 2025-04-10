package cc.mewcraft.wakame.ecs.component

import cc.mewcraft.wakame.ecs.bridge.EComponentType
import com.github.quillraven.fleks.Component
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import net.kyori.adventure.bossbar.BossBar

data class BossBarVisible(
    val bossBar2DurationTick: Object2IntOpenHashMap<BossBar> = Object2IntOpenHashMap(),
) : Component<BossBarVisible> {
    companion object : EComponentType<BossBarVisible>()

    override fun type(): EComponentType<BossBarVisible> = BossBarVisible
}
