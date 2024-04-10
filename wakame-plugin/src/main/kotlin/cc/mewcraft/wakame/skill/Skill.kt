package cc.mewcraft.wakame.skill

import cc.mewcraft.wakame.adventure.Keyed
import cc.mewcraft.wakame.registry.SkillRegistry
import cc.mewcraft.wakame.util.krequire
import net.kyori.adventure.key.Key
import org.spongepowered.configurate.ConfigurationNode
import java.util.UUID

/**
 * Represents a skill "attached" to a player.
 *
 * If a player has skill X, we say that the skill X is attached to that
 * player; By contrast, if the player has no skill, we say that the player
 * has no skill attached.
 */
interface Skill : Keyed {
    /**
     * The unique identifier of this skill.
     */
    val uniqueId: UUID

    /**
     * The trigger of this skill.
     */
    val trigger: Trigger


    enum class Trigger {
        NONE,
        BREAK_BLOCK,
    }

    fun castAt(target: Target.Void) {}
    fun castAt(target: Target.Location) {}
    fun castAt(target: Target.LivingEntity) {}
}

/**
 * A no-op skill. Used as placeholder object.
 */
object NoopSkill : Skill {
    override val uniqueId: UUID = UUID.fromString("1826a767-d424-4024-8b8f-4e66157e35de")
    override val trigger: Skill.Trigger = Skill.Trigger.NONE
    override val key: Key = SkillRegistry.EMPTY_KEY
}

/**
 * A skill that applies its effects by using specific key combinations.
 * The key combinations currently include the alternations of Mouse Left (L)
 * and Mouse Right (R), such as "RRL" and "LLR".
 */
interface ActiveSkill : Skill

/**
 * A skill that applies its effects either permanently as soon as it is
 * available, or activate by itself if the skill is available and its
 * requirements met. These requirements can range from attacking a monster,
 * casting a spell or even getting attacked.
 */
interface PassiveSkill : Skill

/**
 * Creates a skill from the given configuration node. The node must contain
 * a key `type` that specifies the type of the skill template to use.
 */
fun Skill(node: ConfigurationNode): Skill {
    val type = node.node("type").krequire<String>()
    return node.krequire(SkillTemplates.INSTANCE[type])
}
