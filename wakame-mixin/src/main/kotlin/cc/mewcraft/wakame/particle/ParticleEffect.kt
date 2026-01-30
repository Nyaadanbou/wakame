package cc.mewcraft.wakame.particle

import org.bukkit.World

/**
 * 粒子效果对象，存储在 [org.bukkit.World] 的 Metadata 中.
 */
data class ParticleEffect(
    var world: World,
    val configs: MutableList<ParticleConfiguration>,
) {
    constructor(
        world: World,
        vararg particleConfigurations: ParticleConfiguration,
    ) : this(world, particleConfigurations.toMutableList())
}