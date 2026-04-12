package cc.mewcraft.wakame.hook.impl.mythicmobs.mechanic

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.damage.*
import cc.mewcraft.wakame.entity.attribute.AttributeMapAccess
import cc.mewcraft.wakame.hook.impl.mythicmobs.MythicUtils
import cc.mewcraft.wakame.util.decorate
import io.lumine.mythic.api.adapters.AbstractEntity
import io.lumine.mythic.api.config.MythicLineConfig
import io.lumine.mythic.api.skills.ITargetedEntitySkill
import io.lumine.mythic.api.skills.SkillMetadata
import io.lumine.mythic.api.skills.SkillResult
import io.lumine.mythic.api.skills.ThreadSafetyLevel
import io.lumine.mythic.api.skills.placeholders.PlaceholderDouble
import io.lumine.mythic.bukkit.MythicBukkit
import io.lumine.mythic.core.mobs.ActiveMob
import io.lumine.mythic.core.skills.SkillExecutor
import io.lumine.mythic.core.skills.SkillMechanic
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import java.io.File
import kotlin.jvm.optionals.getOrNull

class DamageAttributeMapMechanic(
    manager: SkillExecutor,
    file: File,
    line: String,
    mlc: MythicLineConfig,
) : SkillMechanic(manager, file, line, mlc), ITargetedEntitySkill {

    companion object {
        private val logger = LOGGER.decorate(DamageAttributeMapMechanic::class)
    }

    /**
     * 属性来源. 决定从哪个实体获取属性来计算伤害.
     */
    private enum class AttributeSource {
        /** 使用施法者自身的属性. */
        CASTER,

        /** 使用施法者的 parent (召唤者) 的属性. */
        PARENT,
    }

    /**
     * 属性来源. 决定使用哪个实体的属性来计算伤害.
     * - `CASTER` (默认): 使用施法者自身的属性.
     * - `PARENT`: 使用施法者的 parent (召唤者) 的属性.
     */
    private val attributeSource: AttributeSource = mlc.getEnum(arrayOf("source", "src"), AttributeSource::class.java, AttributeSource.CASTER)
    /**
     * 当 [attributeSource] 为 [AttributeSource.PARENT] 时, 沿 parent 链向上查找的层数.
     * `1` 表示直接 parent, `2` 表示 parent 的 parent, 以此类推. 默认 `1`, 最大 `3`.
     */
    private val sourceDepth: Int = mlc.getInteger(arrayOf("source_depth", "sd"), 1).coerceIn(1, 8)
    /**
     * 用于控制最终伤害数值占面板数值的百分比. `1.0` 为 100%, 以此类推. 默认 `1.0`.
     */
    private val percent: PlaceholderDouble = mlc.getPlaceholderDouble(arrayOf("percent", "p"), 1.0)
    /**
     * @see DamageMetadata.ignoreBlocking
     */
    private val ignoreBlocking: Boolean = mlc.getBoolean(arrayOf("ignore_blocking", "ib"), false)
    /**
     * @see DamageMetadata.ignoreResistance
     */
    private val ignoreResistance: Boolean = mlc.getBoolean(arrayOf("ignore_resistance", "ir"), false)
    /**
     * @see DamageMetadata.ignoreAbsorption
     */
    private val ignoreAbsorption: Boolean = mlc.getBoolean(arrayOf("ignore_absorption", "ia"), false)
    /**
     * 是否造成击退效果. 默认 `true`.
     */
    private val knockback: Boolean = mlc.getBoolean(arrayOf("knockback", "kb"), true)

    init {
        threadSafetyLevel = ThreadSafetyLevel.SYNC_ONLY
    }

    /**
     * 根据 [attributeSource] 配置, 从 [SkillMetadata] 中解析出用于计算伤害的源实体.
     *
     * @return 源 [LivingEntity], 或 `null` 表示无法解析
     */
    private fun resolveSourceEntity(data: SkillMetadata): LivingEntity? {
        return when (attributeSource) {
            AttributeSource.CASTER -> {
                data.caster?.entity?.bukkitEntity as? LivingEntity
            }

            AttributeSource.PARENT -> {
                val casterMob = data.caster as? ActiveMob ?: run {
                    logger.warn("Caster is not an ActiveMob, cannot resolve parent")
                    return null
                }
                resolveNthParent(casterMob, sourceDepth, currentDepth = 1)
            }
        }
    }

    /**
     * 递归沿 parent 链向上查找第 [targetDepth] 层的 parent 实体.
     *
     * @param mob 当前层的 [ActiveMob]
     * @param targetDepth 目标深度 (总共要往上几层)
     * @param currentDepth 当前已到达的深度 (从 1 开始)
     * @return 目标层的 [LivingEntity], 或 `null` 表示链中断
     */
    private fun resolveNthParent(mob: ActiveMob, targetDepth: Int, currentDepth: Int): LivingEntity? {
        val parentEntity = mob.parent.orElse(null) ?: run {
            logger.warn("Cannot resolve parent at depth $currentDepth/$targetDepth: no parent found")
            return null
        }
        val parentLiving = parentEntity.bukkitEntity as? LivingEntity ?: run {
            logger.warn("Parent at depth $currentDepth/$targetDepth is not a LivingEntity")
            return null
        }
        // 已到达目标深度, 返回结果
        if (currentDepth >= targetDepth) {
            return parentLiving
        }
        // 还需继续往上, 中间层的 parent 必须也是 ActiveMob
        val parentMob = asActiveMob(parentEntity) ?: run {
            logger.warn("Parent at depth $currentDepth/$targetDepth is not an ActiveMob, cannot traverse further")
            return null
        }
        return resolveNthParent(parentMob, targetDepth, currentDepth + 1)
    }

    /**
     * 尝试将 [AbstractEntity] 解析为 [ActiveMob].
     */
    private fun asActiveMob(entity: AbstractEntity): ActiveMob? {
        return MythicBukkit.inst().mobManager.getActiveMob(entity.uniqueId).getOrNull()
    }

    override fun castAtEntity(data: SkillMetadata, target: AbstractEntity): SkillResult {
        if (target.isDead) {
            return SkillResult.INVALID_TARGET
        }
        val targetEntity = target.bukkitEntity as? LivingEntity ?: return SkillResult.INVALID_TARGET

        // 获取施法者实体 (用于 DamageSource, 即伤害归属)
        val casterEntity = data.caster?.entity?.bukkitEntity as? LivingEntity ?: run {
            logger.warn("Caster is not a living entity, aborting execution")
            return SkillResult.ERROR
        }

        // 根据配置解析属性来源实体 (用于读取 AttributeContainer)
        val sourceEntity = resolveSourceEntity(data) ?: run {
            logger.warn("Failed to resolve attribute source entity (source=$attributeSource), aborting execution")
            return SkillResult.ERROR
        }

        val damageSource = when (casterEntity) {
            is Player -> KoishDamageSources.playerAttack(casterEntity)
            else -> KoishDamageSources.mobAttack(casterEntity)
        }
        val attributeContainer = AttributeMapAccess.get(sourceEntity).getOrNull() ?: run {
            logger.warn("No AttributeMap found for source entity $sourceEntity (source=$attributeSource), aborting execution")
            return SkillResult.ERROR
        }
        val damageBundle = damageBundle(attributeContainer) {
            every {
                standard()
                rate {
                    standard() * percent[data]
                }
            }
        }
        val mythicDamageMetadata = MythicUtils.createMythicDamageMetadata(data)
        // 添加标记使本次伤害被视为技能伤害, 而不再触发 ~onAttack 触发器, 避免堆栈溢出
        target.setMetadata("skill-damage", mythicDamageMetadata)
        // 对目标生物造成自定义的萌芽伤害
        val csm = CriticalStrikeMetadata(attributeContainer)
        val ib = ignoreBlocking
        val ir = ignoreResistance
        val ia = ignoreAbsorption
        val damageMetadata = DamageMetadata(damageBundle, csm, ib, ir, ia)
        DamageManagerApi.hurt(targetEntity, damageMetadata, damageSource, knockback)
        // 移除标记 (是的, MythicMobs 并不会自动移除伤害元数据)
        target.removeMetadata("skill-damage")
        return SkillResult.SUCCESS
    }
}
