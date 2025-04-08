@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.ecs.component

import cc.mewcraft.wakame.ecs.bridge.EComponent
import cc.mewcraft.wakame.ecs.bridge.EComponentType
import cc.mewcraft.wakame.ecs.data.ParticleConfiguration
import org.bukkit.World

/**
 * 在指定的 [World] 上显示粒子效果.
 *
 * @see cc.mewcraft.wakame.ecs.system.ParticleSystem
 */
data class ParticleEffectComponent(
    var world: World,
    val configs: MutableList<ParticleConfiguration>,
) : EComponent<ParticleEffectComponent> {
    constructor(world: World, vararg particleConfigurations: ParticleConfiguration) : this(world, particleConfigurations.toMutableList())

    companion object : EComponentType<ParticleEffectComponent>()

    override fun type(): EComponentType<ParticleEffectComponent> = ParticleEffectComponent
}