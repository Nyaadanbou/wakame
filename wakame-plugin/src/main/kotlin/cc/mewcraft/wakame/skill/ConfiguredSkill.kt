package cc.mewcraft.wakame.skill

import cc.mewcraft.wakame.config.NodeConfigProvider
import cc.mewcraft.wakame.event.PlayerSkillPrepareCastEvent
import cc.mewcraft.wakame.event.SkillPrepareCastEvent
import cc.mewcraft.wakame.registry.SkillRegistry
import cc.mewcraft.wakame.skill.condition.EmptySkillConditionGroup
import cc.mewcraft.wakame.skill.condition.PlayerSkillCastContext
import cc.mewcraft.wakame.skill.condition.SkillCastContext
import cc.mewcraft.wakame.skill.condition.SkillConditionGroup
import cc.mewcraft.wakame.skill.condition.EmptySkillConditionGroup
import cc.mewcraft.wakame.skill.condition.PlayerSkillCastContext
import cc.mewcraft.wakame.skill.condition.SkillCastContext
import cc.mewcraft.wakame.skill.condition.SkillConditionGroup
import cc.mewcraft.wakame.skill.type.SkillType
import cc.mewcraft.wakame.util.krequire
import org.spongepowered.configurate.ConfigurationNode

/**
 * Represents a skill "attached" to a player.
 *
 * If a player has skill X, we say that the skill X is attached to that
 * player; By contrast, if the player has no skill, we say that the player
 * has no skill attached.
 */
interface ConfiguredSkill {
    /**
     * The conditions that must be met in order to cast this skill.
     *
     * @see SkillConditionGroup
     */
    val conditions: SkillConditionGroup

    fun cast(context: SkillCastContext) = Unit
}

/**
 * A no-op skill. Used as placeholder object.
 */
object NoopConfiguredSkill : ConfiguredSkill {
    override val conditions: SkillConditionGroup = EmptySkillConditionGroup
}

/**
 * A skill that applies its effects by using specific key combinations.
 * The key combinations currently include the alternations of Mouse Left (L)
 * and Mouse Right (R), such as "RRL" and "LLR".
 */
interface ActiveConfiguredSkill : ConfiguredSkill

/**
 * A skill that applies its effects either permanently as soon as it is
 * available, or activate by itself if the skill is available and its
 * requirements met. These requirements can range from attacking a monster,
 * casting a spell or even getting attacked.
 */
interface PassiveConfiguredSkill : ConfiguredSkill

/**
 * Creates a skill from the given configuration node. The node must contain
 * a key `type` that specifies the type of the skill template to use.
 *
 * @param node The configuration node to create the skill from.
 * @param relPath The relative path of the configuration node.
 */
fun ConfiguredSkill(node: ConfigurationNode, relPath: String): ConfiguredSkill {
    val type = node.node("type").krequire<String>()
    val provider = NodeConfigProvider(node, relPath)
    return SkillRegistry.SKILL_TYPES[type].create(provider)
}

fun ConfiguredSkill.tryCast(skillCastContext: SkillCastContext) {
    val event: SkillPrepareCastEvent
    when (skillCastContext) {
        is PlayerSkillCastContext -> {
            event = PlayerSkillPrepareCastEvent(
                this,
                skillCastContext
            )
        }

        else -> {
            return // TODO 其他释放技能的情况
        }
    }
    // 这里允许其他模块监听事件，修改上下文，从而对技能的释放产生影响
    event.callEvent()
    if (event.isCancelled) return
    val conditionGroup = this.conditions
    val context = event.skillCastContext
    if (conditionGroup.test(context)) {
        try {
            this.cast(context)
            conditionGroup.cost(context)
        } catch (e: Throwable) {
            if (e is SkillCannotCastException) {
                event.skillCastContext.caster.sendMessage(e.beautify())
            } else {
                throw e
            }
        }
    } else {
        conditionGroup.notifyFailure(context)
    }
}