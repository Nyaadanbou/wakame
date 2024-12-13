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
) : Component<ParticleEffectComponent> {
    override fun type(): ComponentType<ParticleEffectComponent> = ParticleEffectComponent

    companion object : ComponentType<ParticleEffectComponent>()
}