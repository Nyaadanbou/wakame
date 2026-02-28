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
import io.lumine.mythic.core.skills.SkillExecutor
import io.lumine.mythic.core.skills.SkillMechanic
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import java.io.File

class DamageAttributeMapMechanic(
    manager: SkillExecutor,
    file: File,
    line: String,
    mlc: MythicLineConfig,
) : SkillMechanic(manager, file, line, mlc), ITargetedEntitySkill {

    companion object {
        private val logger = LOGGER.decorate(DamageAttributeMapMechanic::class)
    }

    init {
        threadSafetyLevel = ThreadSafetyLevel.SYNC_ONLY
    }

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

    override fun castAtEntity(data: SkillMetadata, target: AbstractEntity): SkillResult {
        if (target.isDead) {
            return SkillResult.INVALID_TARGET
        }
        val targetEntity = target.bukkitEntity as? LivingEntity ?: return SkillResult.INVALID_TARGET
        val casterEntity = data.caster?.entity?.bukkitEntity as? LivingEntity ?: run {
            logger.warn("Caster is not a living entity, aborting execution")
            return SkillResult.ERROR
        }
        val damageSource = when (casterEntity) {
            is Player -> KoishDamageSources.playerAttack(casterEntity)
            else -> KoishDamageSources.mobAttack(casterEntity)
        }
        val attributeContainer = AttributeMapAccess.INSTANCE.get(casterEntity).getOrNull() ?: run {
            logger.warn("No AttributeMap found for caster $casterEntity, aborting execution")
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
