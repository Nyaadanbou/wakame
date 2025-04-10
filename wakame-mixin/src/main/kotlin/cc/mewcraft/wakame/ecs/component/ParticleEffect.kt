@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.ecs.component

import cc.mewcraft.wakame.ecs.bridge.EComponent
import cc.mewcraft.wakame.ecs.bridge.EComponentType
import cc.mewcraft.wakame.ecs.data.ParticleConfiguration
import org.bukkit.World

/**
 * 在指定的 [World] 上显示粒子效果.
 *
 * @see cc.mewcraft.wakame.ecs.system.RenderParticle
 */
data class ParticleEffect(
    var world: World,
    val configs: MutableList<ParticleConfiguration>,
) : EComponent<ParticleEffect> {
    constructor(world: World, vararg particleConfigurations: ParticleConfiguration) : this(world, particleConfigurations.toMutableList())

    companion object : EComponentType<ParticleEffect>()

    override fun type(): EComponentType<ParticleEffect> = ParticleEffect
}