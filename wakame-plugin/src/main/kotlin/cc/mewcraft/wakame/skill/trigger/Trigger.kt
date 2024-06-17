package cc.mewcraft.wakame.skill.trigger

import cc.mewcraft.wakame.Namespaces
import cc.mewcraft.wakame.SchemaSerializer
import cc.mewcraft.wakame.adventure.Keyed
import cc.mewcraft.wakame.registry.SkillRegistry
import cc.mewcraft.wakame.util.Key
import cc.mewcraft.wakame.util.toSimpleString
import net.kyori.adventure.key.Key
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import org.spongepowered.configurate.ConfigurationNode
import java.lang.reflect.Type
import java.util.stream.Stream

/**
 * 代表一个可以触发技能的玩家操作.
 *
 * 这是一个顶级接口.
 */
sealed interface Trigger : Examinable, Keyed

/**
 * 代表一个由单个按键输入完成的操作.
 */
enum class SingleTrigger(
    /**
     * The identifier of the trigger.
     *
     * This is used for concatenation of triggers in a [SequenceTrigger].
     */
    val id: Char,

    /**
     * 用来在配置文件里指定该 [SingleTrigger].
     */
    override val key: Key,
) : Trigger {
    /**
     * // FIXME 明确自身的定义, 明确 SingleTrigger 用于 SequenceTrigger 时的一些注意事项
     */
    LEFT_CLICK('0', Key(Namespaces.TRIGGER, "generic/left_click")),

    /**
     *
     */
    RIGHT_CLICK('1', Key(Namespaces.TRIGGER, "generic/right_click")),

    /**
     *
     */
    ATTACK('2', Key(Namespaces.TRIGGER, "generic/attack")),

    /**
     *
     */
    JUMP('3', Key(Namespaces.TRIGGER, "generic/jump")),

    /**
     *
     */
    NOOP('4', Key(Namespaces.TRIGGER, "generic/noop")),
    ;

    override fun examinableProperties(): Stream<out ExaminableProperty> {
        return Stream.of(
            ExaminableProperty.of("id", id),
            ExaminableProperty.of("name", name),
        )
    }

    override fun toString(): String {
        return toSimpleString()
    }
}

/**
 * 代表一个由多个按键输入组成的操作, 也就是该触发器要求玩家按顺序触发指定的 [SingleTrigger].
 */
interface SequenceTrigger : Trigger {
    val triggers: List<SingleTrigger>

    companion object {
        /**
         * 从给定的 [序列][sequence] 创建一个 [SequenceTrigger].
         */
        fun of(sequence: List<SingleTrigger>): SequenceTrigger {
            return SequenceTriggerImpl(sequence)
        }

        /**
         * 从给定的 [triggers] 生成所有可能的 [SequenceTrigger].
         *
         * 例如 [triggers] 为: `[0, 1]`, [length] 为 `3`, 那么将生成 `2^3=8` 个 [SequenceTrigger]:
         * `000`, `001`, `010`, `011`, `100`, `101`, `110`, `111`.
         */
        fun generate(triggers: List<SingleTrigger>, length: Int): List<SequenceTrigger> {
            val results = mutableListOf<SequenceTrigger>()

            fun generate(currentCombo: List<SingleTrigger>) {
                if (currentCombo.size == length) {
                    results.add(SequenceTriggerImpl(currentCombo))
                    return
                }

                for (i in triggers.indices) {
                    generate(currentCombo + triggers[i])
                }
            }

            generate(emptyList())
            return results
        }
    }
}

private class SequenceTriggerImpl(
    override val triggers: List<SingleTrigger>,
) : SequenceTrigger {
    override val key: Key = Key(Namespaces.TRIGGER, "combo/${triggers.map { it.id }.joinToString("")}")

    override fun equals(other: Any?): Boolean {
        if (this === other)
            return true
        if (other !is SequenceTrigger)
            return false
        if (triggers.size != other.triggers.size)
            return false
        triggers.forEachIndexed { index, trigger ->
            if (trigger != other.triggers[index]) {
                return false
            }
        }
        return true
    }

    override fun hashCode(): Int {
        return triggers.fold(0) { hash, trigger -> 31 * hash + trigger.hashCode() }
    }
}

internal object TriggerSerializer : SchemaSerializer<Trigger> {
    override fun deserialize(type: Type, node: ConfigurationNode): Trigger {
        val key = Key(node.string.orEmpty())
        return SkillRegistry.TRIGGERS[key]
    }
}