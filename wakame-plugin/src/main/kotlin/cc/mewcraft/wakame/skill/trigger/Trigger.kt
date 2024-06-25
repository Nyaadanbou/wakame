package cc.mewcraft.wakame.skill.trigger

import cc.mewcraft.wakame.Namespaces
import cc.mewcraft.wakame.SchemaSerializer
import cc.mewcraft.wakame.adventure.key.Keyed
import cc.mewcraft.wakame.registry.SkillRegistry
import cc.mewcraft.wakame.util.Key
import cc.mewcraft.wakame.util.toSimpleString
import net.kyori.adventure.key.Key
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.SerializationException
import java.lang.reflect.Type
import java.util.stream.Stream

/**
 * 代表一个可以触发技能的玩家操作.
 *
 * 这是一个顶级接口.
 */
sealed interface Trigger : Keyed, Examinable

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
     * 代表玩家按下了左键, 具体上是指左键空气与方块的交互.
     *
     * 不包括对生物的左键攻击.
     */
    LEFT_CLICK('0', Key(Namespaces.TRIGGER, "generic/left_click")),

    /**
     * 代表玩家按下了右键, 具体上是指右键空气与方块的交互.
     *
     * 不包括对生物的右键交互.
     */
    RIGHT_CLICK('1', Key(Namespaces.TRIGGER, "generic/right_click")),

    /**
     * 代表玩家按下了攻击键, 具体上是指左键对生物的攻击.
     *
     * 不包括对空气与方块的交互.
     */
    ATTACK('2', Key(Namespaces.TRIGGER, "generic/attack")),

    /**
     * 代表玩家按下了跳跃键.
     */
    JUMP('3', Key(Namespaces.TRIGGER, "generic/jump")),

    /**
     * 玩家进行了移动操作.
     */
    MOVE('4', Key(Namespaces.TRIGGER, "generic/walk")),

    /**
     * 代表玩家没有进行任何操作.
     */
    NOOP(Char.MIN_VALUE, Key(Namespaces.TRIGGER, "generic/noop")),
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
    /**
     * 组成该触发器序列的触发器 (按顺序).
     */
    val triggers: List<SingleTrigger>

    companion object {
        /**
         * 从给定的 [序列][sequence] 创建一个 [SequenceTrigger].
         */
        fun of(sequence: List<SingleTrigger>): SequenceTrigger {
            return Impl(sequence)
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
                    results.add(Impl(currentCombo))
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

    private class Impl(
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
}

internal object SkillTriggerSerializer : SchemaSerializer<Trigger> {
    override fun deserialize(type: Type, node: ConfigurationNode): Trigger {
        val raw = node.string ?: throw SerializationException(node, type, "Skill trigger cannot be an empty string")
        val key = Key(raw)
        val trigger = SkillRegistry.TRIGGERS.find(key) ?: throw SerializationException(node, type, "Cannot find skill trigger with key: '$key'")
        return trigger
    }
}