package cc.mewcraft.wakame.compatibility.mythicmobs.mechanic

import io.lumine.mythic.api.adapters.AbstractEntity
import io.lumine.mythic.api.config.MythicLineConfig
import io.lumine.mythic.api.skills.ITargetedEntitySkill
import io.lumine.mythic.api.skills.SkillMetadata
import io.lumine.mythic.api.skills.SkillResult
import io.lumine.mythic.api.skills.placeholders.PlaceholderDouble
import io.lumine.mythic.core.skills.SkillExecutor
import io.lumine.mythic.core.skills.SkillMechanic
import net.kyori.examination.Examinable
import org.bukkit.entity.LivingEntity
import org.koin.core.component.KoinComponent
import java.io.File


class NekoDamageMechanic(
    manager: SkillExecutor,
    file: File,
    line: String,
    mlc: MythicLineConfig,
) : SkillMechanic(manager, file, line, mlc), ITargetedEntitySkill {
    companion object {
        private val DAMAGE_BUNDLE_PATTERN: Regex = Regex("\\([^)]+\\)")
    }

    private val damageBundle: DamageBundle = parseDamageBundle(mlc.getStringList(arrayOf("bundle", "b"), ""))
    private val tags: DamageTags = parseDamageTags(mlc.getStringList(arrayOf("tags", "t"), ""))
    private val criticalStrikePower: PlaceholderDouble = mlc.getPlaceholderDouble(arrayOf("critical_strike_power", "csp"), .0)
    private val criticalStrikeState: CriticalStrikeState = parseCriticalState(mlc.getString(arrayOf("critical_strike_state", "css"), "NONE"))

    private fun parseDamageBundle(origin: List<String>): DamageBundle {
        return DamageBundle(
            origin.associate {
                val split = DAMAGE_BUNDLE_PATTERN.find(it)
                    ?.value // 提取括号内的内容 (包括括号)
                    ?.removeSurrounding("(", ")") // 去除括号
                    ?.split(",") // 按逗号分割
                    ?: throw IllegalArgumentException("Invalid damage bundle: $it")
                when (split.size) {
                    // 登神长阶
                    // 指定 element, min, max, rate, defense_penetration, defense_penetration_rate
                    2 -> split[0] to DamagePacket(split[0], split[1].toDouble(), split[1].toDouble(), 1.0, .0, .0)
                    3 -> split[0] to DamagePacket(split[0], split[1].toDouble(), split[2].toDouble(), 1.0, .0, .0)
                    4 -> split[0] to DamagePacket(split[0], split[1].toDouble(), split[2].toDouble(), split[3].toDouble(), .0, .0)
                    5 -> split[0] to DamagePacket(split[0], split[1].toDouble(), split[2].toDouble(), split[3].toDouble(), split[4].toDouble(), .0)
                    6 -> split[0] to DamagePacket(split[0], split[1].toDouble(), split[2].toDouble(), split[3].toDouble(), split[4].toDouble(), split[5].toDouble())
                    else -> throw IllegalArgumentException("Invalid damage bundle: $it")
                }
            }
        )
    }

    private fun parseDamageTags(origin: List<String>): DamageTags {
        if (origin.isEmpty())
            return DamageTags()
        return DamageTags(origin.map { DamageTag.valueOf(it) })
    }

    private fun parseCriticalState(origin: String): CriticalStrikeState {
        return CriticalStrikeState.valueOf(origin)
    }

    override fun castAtEntity(data: SkillMetadata, target: AbstractEntity): SkillResult {
        if (target.isDead)
            return SkillResult.INVALID_TARGET
        val entity = target.bukkitEntity as? LivingEntity
            ?: return SkillResult.INVALID_TARGET

        val damageMetadata = DamageMetadata2(tags, damageBundle, CriticalStrikeMetadata(criticalStrikePower[target], criticalStrikeState))
        entity.hurt(damageMetadata)

        return SkillResult.SUCCESS
    }
}





data class DamageMetadata2(
    val damageTags: DamageTags,
    val damageBundle: DamageBundle,
    val criticalStrikeMetadata: CriticalStrikeMetadata,
)

data class CriticalStrikeMetadata(
    /**
     * 暴击倍率的值.
     */
    val power: Double,

    /**
     * 这次伤害的暴击状态.
     */
    val state: CriticalStrikeState,
)

/**
 * 伤害捆绑包, 封装了一个或多个 [DamagePacket].
 */
data class DamageBundle(
    val packets: Map<String, DamagePacket>,
) : Examinable, KoinComponent

/**
 * 伤害包, 包含了一种特定元素的伤害信息.
 */
data class DamagePacket(
    /**
     * 伤害的元素类型.
     */
    val element: String,

    /**
     * 伤害的最小值.
     */
    val min: Double,

    /**
     * 伤害的最大值.
     */
    val max: Double,

    /**
     * 伤害的加成比率. 例如: 火元素伤害+50%.
     */
    val rate: Double,

    /**
     * 伤害的护甲穿透值.
     */
    val defensePenetration: Double,

    /**
     * 伤害的护甲穿透率.
     */
    val defensePenetrationRate: Double,
)

data class DamageTags(
    val tags: List<DamageTag> = emptyList(),
)

/**
 * 伤害标签.
 */
enum class DamageTag {
    /**
     * 标记近战类型的伤害.
     */
    MELEE,

    /**
     * 标记弹药类型的伤害.
     */
    PROJECTILE,

    /**
     * 标记魔法类型的伤害.
     */
    MAGIC,

    /**
     * 标记范围伤害武器非本体攻击造成的伤害.
     */
    EXTRA,

    /**
     * 标记玩家徒手造成的伤害.
     * 或相当于徒手攻击的物品造成的伤害.
     */
    HAND,

    /**
     * 标记原版剑类武器的伤害.
     */
    SWORD,

    /**
     * 标记原版斧类武器的伤害.
     */
    AXE,

    /**
     * 标记原版弓类武器的伤害.
     */
    BOW,

    /**
     * 标记原版弩类武器的伤害.
     */
    CROSSBOW,

    /**
     * 标记原版三叉戟类武器的伤害.
     */
    TRIDENT,

    /**
     * 标记原版重锤类武器的伤害.
     */
    MACE,

    /**
     * 标记锤类武器的伤害.
     */
    HAMMER,

    /**
     * 标记矛类武器的伤害.
     */
    SPEAR,

    /**
     * 标记镰刀类武器的伤害.
     */
    SICKLE,

    /**
     * 标记法杖类武器的伤害.
     */
    WAND,
}

/**
 * 暴击状态.
 */
enum class CriticalStrikeState {
    /**
     * 正暴击.
     */
    POSITIVE,

    /**
     * 负暴击.
     */
    NEGATIVE,

    /**
     * 无暴击.
     */
    NONE
}

fun LivingEntity.hurt(metadata: DamageMetadata2) {
    println("LivingEntity ${this.name} hurt by $metadata")
}