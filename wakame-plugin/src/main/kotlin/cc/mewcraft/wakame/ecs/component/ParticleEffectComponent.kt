@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.ecs.component

import cc.mewcraft.wakame.ecs.bridge.BukkitWorld
import cc.mewcraft.wakame.ecs.data.ParticleConfiguration
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

/**
 * 在指定的 [BukkitWorld] 上显示粒子效果.
 *
 * @see cc.mewcraft.wakame.ecs.system.ParticleSystem
 */
data class ParticleEffectComponent(
    var bukkitWorld: BukkitWorld,
    val particleConfigurations: MutableList<ParticleConfiguration>,
) : Component<ParticleEffectComponent> {
    companion object : ComponentType<ParticleEffectComponent>()

    constructor(bukkitWorld: BukkitWorld, vararg particleConfigurations: ParticleConfiguration) : this(bukkitWorld, particleConfigurations.toMutableList())

    override fun type(): ComponentType<ParticleEffectComponent> = ParticleEffectComponent
}