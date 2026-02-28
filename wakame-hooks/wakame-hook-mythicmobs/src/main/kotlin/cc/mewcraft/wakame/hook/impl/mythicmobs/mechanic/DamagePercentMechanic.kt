package cc.mewcraft.wakame.hook.impl.mythicmobs.mechanic

import cc.mewcraft.wakame.damage.*
import cc.mewcraft.wakame.entity.attribute.AttributeMapAccess
import cc.mewcraft.wakame.entity.attribute.Attributes
import cc.mewcraft.wakame.hook.impl.mythicmobs.MythicUtils
import io.lumine.mythic.api.adapters.AbstractEntity
import io.lumine.mythic.api.config.MythicLineConfig
import io.lumine.mythic.api.skills.ITargetedEntitySkill
import io.lumine.mythic.api.skills.SkillMetadata
import io.lumine.mythic.api.skills.SkillResult
import io.lumine.mythic.api.skills.ThreadSafetyLevel
import io.lumine.mythic.api.skills.placeholders.PlaceholderDouble
import io.lumine.mythic.core.skills.SkillExecutor
import io.lumine.mythic.core.skills.SkillMechanic
import org.bukkit.damage.DamageSource
import org.bukkit.damage.DamageType
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import java.io.File

class DamagePercentMechanic(
    manager: SkillExecutor,
    file: File,
    line: String,
    mlc: MythicLineConfig,
) : SkillMechanic(manager, file, line, mlc), ITargetedEntitySkill {
    companion object {
        private val DAMAGE_BUNDLE_PATTERN: Regex = Regex("\\([^)]+\\)")
    }

    init {
        threadSafetyLevel = ThreadSafetyLevel.SYNC_ONLY
    }

    private val damageBundle: (SkillMetadata, AbstractEntity) -> DamageBundle = parseDamageBundle(mlc.getStringList(arrayOf("bundle", "b"), ""))
    private val criticalStrikePower: PlaceholderDouble = mlc.getPlaceholderDouble(arrayOf("critical_strike_power", "csp"), 1.0)
    private val criticalStrikeState: CriticalStrikeState = parseCriticalState(mlc.getString(arrayOf("critical_strike_state", "css"), "NONE"))
    private val knockback: Boolean = mlc.getBoolean(arrayOf("knockback", "kb"), true)
    private val currentHealth: Boolean = mlc.getBoolean(arrayOf("current_health", "current", "ch", "c"), false);

    private fun parseDamageBundle(origin: List<String>): (SkillMetadata, AbstractEntity) -> DamageBundle {
        return { data, target ->
            DamageBundleFactory.INSTANCE.createUnsafe(
                origin.associate { rawPacketString ->
                    val split = DAMAGE_BUNDLE_PATTERN.find(rawPacketString)
                        ?.value // 提取括号内的内容 (包括括号)
                        ?.removeSurrounding("(", ")") // 去除括号
                        ?.split(",") // 按逗号分割
                        ?: throw IllegalArgumentException("Invalid damage bundle: $rawPacketString")
                    when (split.size) {
                        // 登神长阶

                        // 配置文件内指定 element, min, 其中 min = max
                        2 -> split[0] to DamagePacket(split[0], split[1].toHealthPercentDouble().invoke(data, target), split[1].toHealthPercentDouble().invoke(data, target))
                        // 配置文件内指定 element, min, max
                        3 -> split[0] to DamagePacket(split[0], split[1].toHealthPercentDouble().invoke(data, target), split[2].toHealthPercentDouble().invoke(data, target))
                        // 配置文件内指定 element, min, max, rate
                        4 -> split[0] to DamagePacket(split[0], split[1].toHealthPercentDouble().invoke(data, target), split[2].toHealthPercentDouble().invoke(data, target), split[3].toPlaceholderDouble()[data])
                        // 配置文件内指定 element, min, max, rate, defense_penetration
                        5 -> split[0] to DamagePacket(
                            split[0],
                            split[1].toHealthPercentDouble().invoke(data, target),
                            split[2].toHealthPercentDouble().invoke(data, target),
                            split[3].toPlaceholderDouble()[data],
                            split[4].toPlaceholderDouble()[data]
                        )
                        // 配置文件内指定 element, min, max, rate, defense_penetration, defense_penetration_rate
                        6 -> split[0] to DamagePacket(
                            split[0],
                            split[1].toHealthPercentDouble().invoke(data, target),
                            split[2].toHealthPercentDouble().invoke(data, target),
                            split[3].toPlaceholderDouble()[data],
                            split[4].toPlaceholderDouble()[data],
                            split[5].toPlaceholderDouble()[data]
                        )

                        else -> throw IllegalArgumentException("Invalid damage packet: '$rawPacketString'")
                    }
                }
            )
        }
    }

    private fun String.toHealthPercentDouble(): (SkillMetadata, AbstractEntity) -> Double {
        return { data, target ->
            val percent = toPlaceholderDouble()[data, target]
            val damage = if (currentHealth) {
                target.health * percent
            } else {
                val maxHealthAttribute = requireNotNull(Attributes.MAX_HEALTH) { "\"max_health\" attribute is null!" }
                val maxHealth = AttributeMapAccess.INSTANCE.get(target.bukkitEntity).getOrNull()?.getValue(maxHealthAttribute) ?: .0
                maxHealth * percent
            }
            damage
        }
    }

    private fun String.toPlaceholderDouble(): PlaceholderDouble {
        return PlaceholderDouble.of(this)
    }

    private fun parseCriticalState(origin: String): CriticalStrikeState {
        return CriticalStrikeState.valueOf(origin)
    }

    override fun castAtEntity(data: SkillMetadata, target: AbstractEntity): SkillResult {
        if (target.isDead) {
            return SkillResult.INVALID_TARGET
        }

        val entity = (target.bukkitEntity as? LivingEntity)
            ?: return SkillResult.INVALID_TARGET

        val damageMetadata = DamageMetadata(
            damageBundle.invoke(data, target), CriticalStrikeMetadata(criticalStrikePower[target], criticalStrikeState)
        )

        val casterEntity = data.caster?.entity?.bukkitEntity as? LivingEntity
        val damageSource = if (casterEntity == null) {
            DamageSource.builder(DamageType.GENERIC).build()
        } else if (casterEntity is Player) {
            KoishDamageSources.playerAttack(casterEntity)
        } else {
            KoishDamageSources.mobAttack(casterEntity)
        }

        val mythicDamageMetadata = MythicUtils.createMythicDamageMetadata(data)
        // 添加标记使本次伤害被视为技能伤害, 而不再触发 ~onAttack 触发器, 避免堆栈溢出
        target.setMetadata("skill-damage", mythicDamageMetadata)
        // 对目标生物造成自定义的萌芽伤害
        DamageManagerApi.hurt(entity, damageMetadata, damageSource, knockback)
        // 移除标记
        // 是的, MythicMobs 并不会自动移除伤害元数据, 但它却以此来区分伤害是否为技能伤害
        target.removeMetadata("skill-damage")

        return SkillResult.SUCCESS
    }
}
