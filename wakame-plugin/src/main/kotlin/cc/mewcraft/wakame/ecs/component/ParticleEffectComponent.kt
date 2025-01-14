@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.ecs.component

import cc.mewcraft.wakame.ecs.data.ParticleInfo
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

data class ParticleEffectComponent(
    val particleInfos: MutableList<ParticleInfo>,
) : Component<ParticleEffectComponent> {

    constructor(vararg particleInfos: ParticleInfo) : this(particleInfos.toMutableList())

    override fun type(): ComponentType<ParticleEffectComponent> = ParticleEffectComponent

    companion object : ComponentType<ParticleEffectComponent>()
}