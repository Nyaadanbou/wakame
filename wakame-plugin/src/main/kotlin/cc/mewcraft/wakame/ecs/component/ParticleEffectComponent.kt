@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.ecs.component

import cc.mewcraft.wakame.ecs.data.ParticlePath
import com.destroystokyo.paper.ParticleBuilder
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import org.bukkit.Location

data class ParticleEffectComponent(
    var builderProvider: (Location) -> ParticleBuilder, // 粒子效果构建器
    var particlePath: ParticlePath,                     // 粒子路径（可能是直线、圆形等）
    var times: Int = -1,                                // 粒子效果将在播放多少次后结束 (每 tick 一次), 小于 0 表示永不结束
) : Component<ParticleEffectComponent> {
    override fun type(): ComponentType<ParticleEffectComponent> = ParticleEffectComponent

    companion object : ComponentType<ParticleEffectComponent>()
}