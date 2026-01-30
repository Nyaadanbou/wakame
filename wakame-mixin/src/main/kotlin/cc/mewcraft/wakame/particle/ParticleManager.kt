@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.particle

import cc.mewcraft.wakame.util.metadata.metadata
import cc.mewcraft.wakame.util.metadata.metadataKey
import com.destroystokyo.paper.event.server.ServerTickStartEvent
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

/**
 * 粒子管理器，处理每 tick 的粒子渲染.
 */
object ParticleManager : Listener {
    private val PARTICLE_EFFECTS_KEY = metadataKey<MutableList<ParticleEffect>>("particle:effects")

    /**
     * 在指定 [World] 添加粒子效果.
     */
    fun addEffect(world: World, effect: ParticleEffect) {
        val metadata = world.metadata()
        val effects = metadata.getOrNull(PARTICLE_EFFECTS_KEY) ?: mutableListOf<ParticleEffect>().also {
            metadata.put(PARTICLE_EFFECTS_KEY, it)
        }
        effects.add(effect)
    }

    /**
     * 获取指定 [World] 的所有粒子效果.
     */
    fun getEffects(world: World): MutableList<ParticleEffect>? {
        return world.metadata().getOrNull(PARTICLE_EFFECTS_KEY)
    }

    /**
     * 每 tick 更新所有粒子效果.
     */
    @EventHandler
    fun onServerTick(event: ServerTickStartEvent) {
        // 遍历所有世界
        val worlds = Bukkit.getWorlds()
        for (world in worlds) {
            val effects = getEffects(world) ?: continue

            // 使用反向迭代以便安全删除
            val iterator = effects.iterator()
            while (iterator.hasNext()) {
                val particleEffect = iterator.next()

                // 处理每个粒子效果配置
                val configIt = particleEffect.configs.iterator()
                while (configIt.hasNext()) {
                    val particleInfo = configIt.next()

                    // 检查是否已经结束
                    if (particleInfo.times == 0) {
                        configIt.remove()
                        continue
                    }

                    // 遍历每个粒子, 计算其在路径上的位置
                    for (i in 0 until particleInfo.count) {
                        // 计算每个粒子的进度: 在 0 到 1 之间
                        val progress = i / particleInfo.count.toDouble()

                        // 获取粒子在路径上的位置
                        val position = particleInfo.path.positionAtProgress(progress)

                        // 使用 ParticleBuilder 生成粒子效果
                        particleInfo.builder(position.toLocation(particleEffect.world)).spawn()
                    }

                    // 触发成功生成粒子效果后，减少剩余次数
                    particleInfo.times--
                }

                // 如果所有配置都已完成, 移除此粒子效果
                if (particleEffect.configs.isEmpty()) {
                    iterator.remove()
                }
            }

            // 如果世界上没有粒子效果了, 删除 metadata
            if (effects.isEmpty()) {
                world.metadata().remove(PARTICLE_EFFECTS_KEY)
            }
        }
    }
}
