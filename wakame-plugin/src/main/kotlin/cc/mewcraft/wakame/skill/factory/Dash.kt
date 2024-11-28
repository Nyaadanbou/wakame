package cc.mewcraft.wakame.skill.factory

import cc.mewcraft.wakame.skill.Skill
import cc.mewcraft.wakame.skill.SkillBase
import cc.mewcraft.wakame.skill.SkillProvider
import cc.mewcraft.wakame.skill.SkillResult
import cc.mewcraft.wakame.skill2.external.component.TickCount
import cc.mewcraft.wakame.skill2.system.MechanicBukkitEntityMetadataSystem
import cc.mewcraft.wakame.util.krequire
import me.lucko.helper.metadata.Metadata
import me.lucko.helper.metadata.MetadataKey
import net.kyori.adventure.key.Key
import org.bukkit.Material
import org.bukkit.Server
import org.bukkit.block.Block
import org.bukkit.entity.LivingEntity
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.extensions.get
import kotlin.jvm.optionals.getOrNull

const val STARTING_TICK: Long = 10L

interface Dash : Skill {

    /**
     * 冲刺的距离.
     */
    val stepDistance: Double

    /**
     * 移动的 Tick 数
     */
    val duration: Long

    /**
     * 撞到实体后是否能继续冲刺
     */
    val canContinueAfterHit: Boolean

    /**
     * 撞到实体后触发的效果
     */
    val hitEffects: List<SkillProvider>

    /**
     * 判定的间隔.
     */
    val hitInterval: Long

    companion object Factory : SkillFactory<Dash> {
        override fun create(key: Key, config: ConfigurationNode): Dash {
            val stepDistance = config.node("step_distance").krequire<Double>()
            val duration = config.node("duration").get<Long>() ?: 50
            val canContinueAfterHit = config.node("can_continue_after_hit").get<Boolean>() ?: true
            val hitEffect = config.node("hit_effects").get<List<SkillProvider>>() ?: emptyList()
            val hitInterval = config.node("hit_interval").get<Long>() ?: 20
            return Impl(key, config, stepDistance, duration, canContinueAfterHit, hitEffect, hitInterval)
        }
    }

    private class Impl(
        override val key: Key,
        config: ConfigurationNode,
        override val stepDistance: Double,
        override val duration: Long,
        override val canContinueAfterHit: Boolean,
        override val hitEffects: List<SkillProvider>,
        override val hitInterval: Long,
    ) : Dash, SkillBase(key, config) {

        private val triggerConditionGetter: TriggerConditionGetter = TriggerConditionGetter()

        override fun cast(entity: LivingEntity): SkillResult<Dash> {
            return DashResult(this, entity)
        }
    }
}

private class DashResult(
    override val skill: Dash,
    private val entity: LivingEntity,
) : SkillResult<Dash> {
    companion object {
        private val DASH_DAMAGE_KEY: MetadataKey<Int> = MetadataKey.createIntegerKey("dash_damage")
        private val DASH_EFFECT_TIME: MetadataKey<Long> = MetadataKey.createLongKey("dash_effect_time")
    }

    override fun executeCast() {
//        if (!checkConditions())
//            return TickResult.ALL_DONE
        val metadata = Metadata.get(entity).getOrNull() ?: return
        val componentMap = metadata.getOrNull(MechanicBukkitEntityMetadataSystem.COMPONENT_MAP_KEY) ?: return
        val tickCount = componentMap[skill, TickCount.externalKey]?.time ?: .0
        if (tickCount >= skill.duration + STARTING_TICK) {
//            return TickResult.ALL_DONE
            return
        }

        val direction = entity.location.direction.setY(0).normalize()

        val stepDistance = skill.stepDistance
        // 计算每一步的移动向量
        var stepVector = direction.clone().multiply(stepDistance)

        // 检查前方和脚下的方块
        val nextLocation = entity.location.add(stepVector)
        val blockInFront = nextLocation.block
        val blockBelow = nextLocation.clone().add(0.0, -1.0, 0.0).block

        if (!blockInFront.isAccessibleForDash()) {
            // 如果前方有方块，尝试向上移动一格高度
            val blockAboveFront = nextLocation.clone().add(0.0, 1.0, 0.0).block
            if (blockAboveFront.isAccessibleForDash() && blockInFront.location.add(0.0, 1.0, 0.0).block.isAccessibleForDash()) {
                stepVector = stepVector.setY(1.0)
            } else {
                componentMap
                return
            }
        } else {
            stepVector = if (blockBelow.isAccessibleForDash()) {
                // 如果脚下没有方块，尝试向下移动一格高度
                stepVector.setY(-1.0)
            } else {
                // 保持原来的Y轴高度
                stepVector.setY(0.0)
            }
        }

        // 应用速度到玩家对象上
        entity.velocity = stepVector
        if (tickCount >= STARTING_TICK && !entity.hasActiveItem()) {
            return
        }

        if (affectEntityNearby(entity)) {
            metadata.put(DASH_DAMAGE_KEY, metadata.getOrDefault(DASH_DAMAGE_KEY, 0).plus(1))
            if (!skill.canContinueAfterHit) {
                return
            }
        }

//        return TickResult.CONTINUE_TICK
    }

    override fun executeBackswing() {
        val metadata = Metadata.get(entity).getOrNull() ?: return
        val damageCount = metadata[DASH_DAMAGE_KEY].getOrNull()

//        val nekoStack = context[SkillContextKey.NEKO_STACK] ?: return TickResult.ALL_DONE
//        if (nekoStack.isDamageable) {
//            if (nekoStack.damage < nekoStack.maxDamage) {
//                nekoStack.hurtAndBreak(player, damageCount)
//            } else {
//                return TickResult.ALL_DONE
//            }
//        }
//        return TickResult.ALL_DONE
    }

    private fun affectEntityNearby(livingEntity: LivingEntity): Boolean {
        val metadata = Metadata.get(entity).getOrNull() ?: return false
        val entities = livingEntity.getNearbyEntities(2.0, 1.0, 2.0)
        if (entities.isEmpty()) {
            return false
        }
        for (entity in entities) {
            if (entity !is LivingEntity)
                continue
            if (metadata[DASH_EFFECT_TIME].getOrNull()?.let { DashSupport.server.currentTick - it < skill.hitInterval } == true)
                continue

            for (skillProvider in skill.hitEffects) {
                val effect = skillProvider.get()
                effect.cast(entity)
                metadata.put(DASH_EFFECT_TIME, DashSupport.server.currentTick.toLong())
            }
        }
        return true
    }

    private fun Block.isAccessibleForDash(): Boolean {
        return when {
            this.type == Material.AIR -> true
            this.isReplaceable -> true
            this.isLiquid -> true
            !this.isSolid -> true
            else -> false
        }
    }

}

private object DashSupport : KoinComponent {
    val server: Server by inject()
}