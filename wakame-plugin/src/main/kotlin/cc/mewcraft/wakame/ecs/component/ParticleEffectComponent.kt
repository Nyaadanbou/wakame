@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.ecs.component

import cc.mewcraft.wakame.ecs.bridge.BukkitWorld
import cc.mewcraft.wakame.ecs.data.ParticleInfo
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

data class ParticleEffectComponent(
    var bukkitWorld: BukkitWorld,
    val particleInfos: MutableList<ParticleInfo>,
) : Component<ParticleEffectComponent> {
    companion object : ComponentType<ParticleEffectComponent>()

    constructor(bukkitWorld: BukkitWorld, vararg particleInfos: ParticleInfo) : this(bukkitWorld, particleInfos.toMutableList())

    override fun type(): ComponentType<ParticleEffectComponent> = ParticleEffectComponent
}