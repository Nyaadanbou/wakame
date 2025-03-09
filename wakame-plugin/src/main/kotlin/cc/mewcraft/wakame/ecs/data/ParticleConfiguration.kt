package cc.mewcraft.wakame.ecs.data

import com.destroystokyo.paper.ParticleBuilder
import org.bukkit.Location

/**
 * 该配置用来控制一次粒子如何播放.
 *
 * @param builderProvider 粒子效果构建器.
 * @param particlePath 粒子路径. (可能是直线、圆形等)
 * @param amount 粒子效果的个数.
 * @param times 粒子效果将在播放多少次后结束 (每 tick 一次), 小于 0 表示永不结束.
 */
data class ParticleConfiguration(
    val builderProvider: (Location) -> ParticleBuilder,
    val particlePath: ParticlePath,
    val amount: Int,
    var times: Int = -1,
)
