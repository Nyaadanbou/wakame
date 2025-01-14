@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.ecs.system

import cc.mewcraft.wakame.ecs.component.ParticleEffectComponent
import cc.mewcraft.wakame.ecs.component.TargetTo
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family

class ParticleSystem : IteratingSystem(
    family = family { all(ParticleEffectComponent, TargetTo) }
) {
    companion object {
        // 假设每个粒子路径可以分为 N 段，进度基于路径段
        private const val NUMBER_OF_PARTICLES = 100  // 设置每个路径上生成粒子的数量
    }

    override fun onTickEntity(entity: Entity) {
        val particleEffectComponent = entity[ParticleEffectComponent]
        val target = entity[TargetTo].target
        for (particleInfo in particleEffectComponent.particleInfos) {
            val builderProvider = particleInfo.builderProvider
            val particlePath = particleInfo.particlePath

            // 检查是否已经结束
            if (particleInfo.times == 0) {
                // 粒子已经结束，删除粒子效果组件
                entity.configure { it -= ParticleEffectComponent }
                return
            }

            // 遍历每个粒子，计算其在路径上的位置
            for (i in 0 until NUMBER_OF_PARTICLES) {
                // 计算每个粒子的进度：在 0 到 1 之间
                val progress = i / (NUMBER_OF_PARTICLES - 1).toDouble()  // 进度从 0 到 1

                // 获取粒子在路径上的位置
                val position = particlePath.positionAtProgress(progress)

                // 使用 ParticleBuilder 生成粒子效果
                builderProvider.invoke(position.toLocation(target.bukkitLocation.world)).spawn()
            }

            // 触发成功生成粒子效果后，减少剩余次数
            particleInfo.times--
        }
    }
}
