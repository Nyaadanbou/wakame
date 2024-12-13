@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.ecs.system

import cc.mewcraft.wakame.ecs.component.ParticleEffectComponent
import cc.mewcraft.wakame.ecs.component.TargetComponent
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family

class ParticleSystem : IteratingSystem(
    family = family { all(ParticleEffectComponent, TargetComponent) }
) {

    // 假设每个粒子路径可以分为 N 段，进度基于路径段
    private val numberOfParticles = 100  // 设置每个路径上生成粒子的数量

    override fun onTickEntity(entity: Entity) {
        val builderProvider = entity[ParticleEffectComponent].builderProvider
        val particlePath = entity[ParticleEffectComponent].particlePath
        val target = entity[TargetComponent].target

        // 遍历每个粒子，计算其在路径上的位置
        for (i in 0 until numberOfParticles) {
            // 计算每个粒子的进度：在 0 到 1 之间
            val progress = i / (numberOfParticles - 1).toDouble()  // 进度从 0 到 1

            // 获取粒子在路径上的位置
            val position = particlePath.positionAtProgress(progress)

            // 使用 ParticleBuilder 生成粒子效果
            builderProvider.invoke(position.toLocation(target.bukkitLocation.world)).spawn()
        }
    }
}
