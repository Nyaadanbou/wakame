package cc.mewcraft.wakame.compatibility.mythicmobs.mechanic

import cc.mewcraft.wakame.damage.*
import io.lumine.mythic.api.adapters.AbstractEntity
import io.lumine.mythic.api.config.MythicLineConfig
import io.lumine.mythic.api.skills.*
import io.lumine.mythic.api.skills.placeholders.PlaceholderDouble
import io.lumine.mythic.core.skills.SkillExecutor
import io.lumine.mythic.core.skills.SkillMechanic
import org.bukkit.entity.LivingEntity
import org.koin.core.component.KoinComponent
import java.io.File


class NekoDamageMechanic(
    manager: SkillExecutor,
    file: File,
    line: String,
    mlc: MythicLineConfig,
) : SkillMechanic(manager, file, line, mlc), ITargetedEntitySkill, KoinComponent {

    companion object {
        private val DAMAGE_BUNDLE_PATTERN: Regex = Regex("\\([^)]+\\)")
    }

    private val damageTags: DamageTags = parseDamageTags(mlc.getStringList(arrayOf("tags", "t"), ""))
    private val damageBundle: DamageBundle = parseDamageBundle(mlc.getStringList(arrayOf("bundle", "b"), ""))
    private val criticalStrikePower: PlaceholderDouble = mlc.getPlaceholderDouble(arrayOf("critical_strike_power", "csp"), 1.0)
    private val criticalStrikeState: CriticalStrikeState = parseCriticalState(mlc.getString(arrayOf("critical_strike_state", "css"), "NONE"))
    private val knockback: Boolean = mlc.getBoolean("knockback", false)

    private fun parseDamageBundle(origin: List<String>): DamageBundle {
        return DamageBundleFactory.instance().createUnsafe(
            origin.associate { rawPacketString ->
                val split = DAMAGE_BUNDLE_PATTERN.find(rawPacketString)
                    ?.value // 提取括号内的内容 (包括括号)
                    ?.removeSurrounding("(", ")") // 去除括号
                    ?.split(",") // 按逗号分割
                    ?: throw IllegalArgumentException("Invalid damage bundle: $rawPacketString")
                when (split.size) {
                    // 登神长阶

                    // 指定 element, min, max, 其中 min = max
                    2 -> split[0] to DamagePacket(split[0], split[1].toDouble(), split[1].toDouble())
                    // 指定 element, min, max
                    3 -> split[0] to DamagePacket(split[0], split[1].toDouble(), split[2].toDouble())
                    // 指定 element, min, max, rate
                    4 -> split[0] to DamagePacket(split[0], split[1].toDouble(), split[2].toDouble(), split[3].toDouble())
                    // 指定 element, min, max, rate, defense_penetration
                    5 -> split[0] to DamagePacket(split[0], split[1].toDouble(), split[2].toDouble(), split[3].toDouble(), split[4].toDouble())
                    // 指定 element, min, max, rate, defense_penetration, defense_penetration_rate
                    6 -> split[0] to DamagePacket(split[0], split[1].toDouble(), split[2].toDouble(), split[3].toDouble(), split[4].toDouble(), split[5].toDouble())

                    else -> throw IllegalArgumentException("Invalid damage packet: '$rawPacketString'")
                }
            }
        )
    }

    private fun parseDamageTags(origin: List<String>): DamageTags {
        if (origin.isEmpty()) {
            return DamageTagsFactory.instance().create()
        }
        return DamageTagsFactory.instance().create(origin.map { DamageTag.valueOf(it) })
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
            damageTags, damageBundle, CriticalStrikeMetadata(criticalStrikePower[target], criticalStrikeState)
        )

        val casterEntity = data.caster?.entity?.bukkitEntity as? LivingEntity

        // 对目标生物造成自定义的萌芽伤害
        DamageManagerApi.instance().hurt(entity, damageMetadata, casterEntity, knockback)

        return SkillResult.SUCCESS
    }
}
