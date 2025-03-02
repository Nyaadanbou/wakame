@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.ecs.component

import cc.mewcraft.wakame.ecs.data.ParticleInfo
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import org.bukkit.World

data class ParticleEffectComponent(
    var bukkitWorld: World,
    val particleInfos: MutableList<ParticleInfo>,
) : Component<ParticleEffectComponent> {

    constructor(bukkitWorld: World, vararg particleInfos: ParticleInfo) : this(bukkitWorld, particleInfos.toMutableList())

    override fun type(): ComponentType<ParticleEffectComponent> = ParticleEffectComponent

    companion object : ComponentType<ParticleEffectComponent>()
}