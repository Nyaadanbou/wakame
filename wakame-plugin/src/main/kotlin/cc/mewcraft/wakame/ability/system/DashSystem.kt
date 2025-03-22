package cc.mewcraft.wakame.ability.system

import cc.mewcraft.wakame.ability.component.*
import cc.mewcraft.wakame.ability.context.abilityInput
import cc.mewcraft.wakame.ability.data.TickResult
import cc.mewcraft.wakame.ecs.bridge.FleksEntity
import cc.mewcraft.wakame.ecs.bridge.koishify
import cc.mewcraft.wakame.ecs.component.TickCountComponent
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.EntityUpdateContext
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.entity.Entity as BukkitEntity

class DashSystem : IteratingSystem(
    family = family { all(AbilityComponent, CastBy, TargetTo, TickCountComponent, AbilityArchetypeComponent, Dash) }
), ActiveAbilitySystem {
    companion object {
        /**
         * 在 Dash 开始前的准备时间
         */
        private const val STARTING_TICK: Long = 10L
    }

    override fun onTickEntity(entity: Entity) {
        val tickCount = entity[TickCountComponent].tick
        entity.configure {
            it += AbilityTickResultComponent(tick(tickCount, entity))
        }
    }

    context(EntityUpdateContext)
    override fun tickCastPoint(tickCount: Int, entity: FleksEntity): TickResult {
        return TickResult.ADVANCE_TO_NEXT_STATE
    }

    context(EntityUpdateContext)
    override fun tickCast(tickCount: Int, entity: FleksEntity): TickResult {
        val dash = entity[Dash]
        if (tickCount >= dash.duration + STARTING_TICK) {
            // 超过了执行时间, 直接完成技能
            return TickResult.ADVANCE_TO_NEXT_STATE_NO_CONSUME
        }
        val bukkitEntity = entity[CastBy].entityOrPlayer()
        val direction = bukkitEntity.location.direction.setY(0).normalize()
        val stepDistance = dash.stepDistance
        // 计算每一步的移动向量
        var stepVector = direction.clone().multiply(stepDistance)
        // 检查前方和脚下的方块
        val nextLocation = bukkitEntity.location.add(stepVector)
        val blockInFront = nextLocation.block
        val blockBelow = nextLocation.clone().add(0.0, -1.0, 0.0).block

        if (!blockInFront.isAccessible()) {
            // 如果前方有方块，尝试向上移动一格高度
            val blockAboveFront = nextLocation.clone().add(0.0, 1.0, 0.0).block
            if (blockAboveFront.isAccessible() && blockInFront.location.add(0.0, 1.0, 0.0).block.isAccessible()) {
                stepVector = stepVector.setY(1.0)
            } else {
                return TickResult.ADVANCE_TO_NEXT_STATE_NO_CONSUME
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
        bukkitEntity.velocity = stepVector

        if (this.affectEntityNearby(bukkitEntity, entity)) {
            if (!dash.canContinueAfterHit) {
                return TickResult.ADVANCE_TO_NEXT_STATE_NO_CONSUME
            }
        }

        return TickResult.CONTINUE_TICK
    }

    private fun affectEntityNearby(casterEntity: BukkitEntity, entity: FleksEntity): Boolean {
        val dash = entity[Dash]
        val bukkitEntities = casterEntity.getNearbyEntities(2.0, 1.0, 2.0)
        if (bukkitEntities.isEmpty()) {
            return false
        }
        for (bukkitEntity in bukkitEntities) {
            if (bukkitEntity !is LivingEntity)
                continue

            for (ability in dash.hitEffects) {
                val input = if (casterEntity is Player) {
                    if (bukkitEntity is Player) {
                        abilityInput(casterEntity.koishify(), bukkitEntity.koishify())
                    } else {
                        abilityInput(casterEntity.koishify(), bukkitEntity.koishify())
                    }
                } else {
                    if (bukkitEntity is Player) {
                        abilityInput(bukkitEntity.koishify(), casterEntity.koishify())
                    } else {
                        abilityInput(bukkitEntity.koishify(), casterEntity.koishify())
                    }
                }
                ability.cast(input)
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