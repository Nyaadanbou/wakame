package cc.mewcraft.wakame.ability.system

import cc.mewcraft.wakame.ability.archetype.abilitySupport
import cc.mewcraft.wakame.ability.character.CasterAdapter
import cc.mewcraft.wakame.ability.character.TargetAdapter
import cc.mewcraft.wakame.ability.component.Dash
import cc.mewcraft.wakame.ability.context.abilityInput
import cc.mewcraft.wakame.ecs.component.AbilityComponent
import cc.mewcraft.wakame.ecs.component.CastBy
import cc.mewcraft.wakame.ecs.component.IdentifierComponent
import cc.mewcraft.wakame.ecs.component.TargetTo
import cc.mewcraft.wakame.ecs.component.TickCountComponent
import cc.mewcraft.wakame.ecs.component.TickResultComponent
import cc.mewcraft.wakame.ecs.data.TickResult
import cc.mewcraft.wakame.ecs.external.ComponentBridge
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Entity as BukkitEntity

class DashSystem : IteratingSystem(
    family = family { all(AbilityComponent, CastBy, TargetTo, TickCountComponent, IdentifierComponent, Dash) }
), ActiveAbilitySystem {
    companion object {
        /**
         * 在 Dash 开始前的准备时间
         */
        private const val STARTING_TICK: Long = 10L
    }

    override fun onTickEntity(entity: Entity) {
        val tickCount = entity[TickCountComponent].tick
        val result = tick(deltaTime.toDouble(), tickCount, ComponentBridge(entity))
        entity.configure {
            it += TickResultComponent(result)
        }
    }

    override fun tickCastPoint(deltaTime: Double, tickCount: Double, componentBridge: ComponentBridge): TickResult {
        return TickResult.NEXT_STATE
    }

    override fun tickCast(deltaTime: Double, tickCount: Double, componentBridge: ComponentBridge): TickResult = abilitySupport {
        val dash = componentBridge[Dash]
        if (tickCount >= dash.duration + STARTING_TICK) {
            // 超过了执行时间, 直接完成技能
            return@abilitySupport TickResult.NEXT_STATE_NO_CONSUME
        }
        val entity = componentBridge.castByEntity() ?: return@abilitySupport TickResult.RESET_STATE
        val direction = entity.location.direction.setY(0).normalize()
        val stepDistance = dash.stepDistance
        // 计算每一步的移动向量
        var stepVector = direction.clone().multiply(stepDistance)
        // 检查前方和脚下的方块
        val nextLocation = entity.location.add(stepVector)
        val blockInFront = nextLocation.block
        val blockBelow = nextLocation.clone().add(0.0, -1.0, 0.0).block

        if (!blockInFront.isAccessible()) {
            // 如果前方有方块，尝试向上移动一格高度
            val blockAboveFront = nextLocation.clone().add(0.0, 1.0, 0.0).block
            if (blockAboveFront.isAccessible() && blockInFront.location.add(0.0, 1.0, 0.0).block.isAccessible()) {
                stepVector = stepVector.setY(1.0)
            } else {
                return@abilitySupport TickResult.NEXT_STATE_NO_CONSUME
            }
        } else {
            stepVector = if (blockBelow.isAccessible()) {
                // 如果脚下没有方块，尝试向下移动一格高度
                stepVector.setY(-1.0)
            } else {
                // 保持原来的Y轴高度
                stepVector.setY(0.0)
            }
        }

        // 应用速度到玩家对象上
        entity.velocity = stepVector

        if (affectEntityNearby(entity, componentBridge)) {
            if (!dash.canContinueAfterHit) {
                return@abilitySupport TickResult.NEXT_STATE_NO_CONSUME
            }
        }

        return@abilitySupport TickResult.CONTINUE_TICK
    }

    private fun affectEntityNearby(casterEntity: BukkitEntity, componentBridge: ComponentBridge): Boolean {
        val dash = componentBridge[Dash]
        val entities = casterEntity.getNearbyEntities(2.0, 1.0, 2.0)
        if (entities.isEmpty()) {
            return false
        }
        for (entity in entities) {
            if (entity !is LivingEntity)
                continue

            for (ability in dash.hitEffects) {
                val input = abilityInput(CasterAdapter.adapt(casterEntity), TargetAdapter.adapt(entity))
                ability.recordBy(input)
            }
        }
        return true
    }

    private fun Block.isAccessible(): Boolean {
        return when {
            this.type == Material.AIR -> true
            this.isReplaceable -> true
            this.isLiquid -> true
            !this.isSolid -> true
            else -> false
        }
    }
}