package cc.mewcraft.wakame.hook.impl.mythicmobs.mechanic

import cc.mewcraft.wakame.damage.CriticalStrikeMetadata
import cc.mewcraft.wakame.damage.CriticalStrikeState
import cc.mewcraft.wakame.damage.DamageBundle
import cc.mewcraft.wakame.damage.DamageBundleFactory
import cc.mewcraft.wakame.damage.DamageManagerApi
import cc.mewcraft.wakame.damage.DamageMetadata
import cc.mewcraft.wakame.damage.DamagePacket
import cc.mewcraft.wakame.damage.KoishDamageSources
import cc.mewcraft.wakame.util.RecursionGuard
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

class NekoBaseDamageMechanic(
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

    private val damageBundle: (SkillMetadata) -> DamageBundle = parseDamageBundle(mlc.getStringList(arrayOf("bundle", "b"), ""))
    private val criticalStrikePower: PlaceholderDouble = mlc.getPlaceholderDouble(arrayOf("critical_strike_power", "csp"), 1.0)
    private val criticalStrikeState: CriticalStrikeState = parseCriticalState(mlc.getString(arrayOf("critical_strike_state", "css"), "NONE"))
    private val knockback: Boolean = mlc.getBoolean(arrayOf("knockback", "kb"), true)

    private fun parseDamageBundle(origin: List<String>): (SkillMetadata) -> DamageBundle {
        return { data ->
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
                        2 -> split[0] to DamagePacket(split[0], split[1].toPlaceholderDouble()[data], split[1].toPlaceholderDouble()[data])
                        // 配置文件内指定 element, min, max
                        3 -> split[0] to DamagePacket(split[0], split[1].toPlaceholderDouble()[data], split[2].toPlaceholderDouble()[data])
                        // 配置文件内指定 element, min, max, rate
                        4 -> split[0] to DamagePacket(split[0], split[1].toPlaceholderDouble()[data], split[2].toPlaceholderDouble()[data], split[3].toPlaceholderDouble()[data])
                        // 配置文件内指定 element, min, max, rate, defense_penetration
                        5 -> split[0] to DamagePacket(split[0], split[1].toPlaceholderDouble()[data], split[2].toPlaceholderDouble()[data], split[3].toPlaceholderDouble()[data], split[4].toPlaceholderDouble()[data])
                        // 配置文件内指定 element, min, max, rate, defense_penetration, defense_penetration_rate
                        6 -> split[0] to DamagePacket(
                            split[0],
                            split[1].toPlaceholderDouble()[data],
                            split[2].toPlaceholderDouble()[data],
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
            damageBundle.invoke(data), CriticalStrikeMetadata(criticalStrikePower[target], criticalStrikeState)
        )

        val casterEntity = data.caster?.entity?.bukkitEntity as? LivingEntity
        val damageSource = if (casterEntity == null) {
            DamageSource.builder(DamageType.GENERIC).build()
        } else if (casterEntity is Player) {
            KoishDamageSources.playerAttack(casterEntity)
        } else {
            KoishDamageSources.mobAttack(casterEntity)
        }

        // 对目标生物造成自定义的萌芽伤害.
        // 由于愚蠢的mm是监听伤害事件来判断生物进行了攻击, 在某些技能触发器下会产生堆栈溢出.
        // 在不使用mm的勾丝api的情况下, 可通过直接阻断循环调用来解决问题.
        RecursionGuard.with(
            functionName = "hurt", silenceLogs = true
        ) {
            DamageManagerApi.INSTANCE.hurt(entity, damageMetadata, damageSource, knockback)
        }

        return SkillResult.SUCCESS
    }
}
