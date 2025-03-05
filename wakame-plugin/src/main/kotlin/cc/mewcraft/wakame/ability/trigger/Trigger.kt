package cc.mewcraft.wakame.ability.trigger

import cc.mewcraft.wakame.registry2.KoishRegistries
import cc.mewcraft.wakame.util.adventure.toSimpleString
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import java.util.stream.Stream

/**
 * 代表一个可以触发技能的玩家操作.
 *
 * 这是一个顶级接口.
 */
sealed interface Trigger : Examinable {
    /**
     * 用来在配置文件里指定该 [Trigger].
     */
    val id: String
}

/**
 * 代表一个由单个按键输入完成的操作.
 */
enum class SingleTrigger(
    /**
     * 用于拼接成 [SequenceTrigger] 的字符.
     */
    val char: Char,
    override val id: String,
): Trigger {
    /**
     * 代表玩家按下了左键, 具体上是指左键空气与方块的交互.
     *
     * 不包括对生物的左键攻击.
     */
    LEFT_CLICK('0', "generic/left_click"),

    /**
     * 代表玩家按下了右键, 具体上是指右键空气与方块的交互.
     *
     * 不包括对生物的右键交互.
     */
    RIGHT_CLICK('1', "generic/right_click"),

    /**
     * 代表玩家按下了攻击键, 具体上是指左键对生物的攻击.
     *
     * 不包括对空气与方块的交互.
     */
    ATTACK('2', "generic/attack"),

    /**
     * 代表玩家按下了跳跃键.
     */
    JUMP('3', "generic/jump"),

    /**
     * 玩家进行了移动操作, 不包括跳跃.
     */
    MOVE('4', "generic/walk"),

    /**
     * 代表玩家按下了潜行键.
     */
    SNEAK('5', "generic/sneak"),
    ;

    init {
        KoishRegistries.TRIGGER.add(id, this)
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> {
        return Stream.of(
            ExaminableProperty.of("id", char),
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
enum class SequenceTrigger(
    /**
     * 组成该触发器序列的触发器 (按顺序).
     */
    val triggers: List<SingleTrigger>,
) : Trigger {
    LLL(SingleTrigger.LEFT_CLICK, SingleTrigger.LEFT_CLICK, SingleTrigger.LEFT_CLICK),
    LLR(SingleTrigger.LEFT_CLICK, SingleTrigger.LEFT_CLICK, SingleTrigger.RIGHT_CLICK),
    LRL(SingleTrigger.LEFT_CLICK, SingleTrigger.RIGHT_CLICK, SingleTrigger.LEFT_CLICK),
    LRR(SingleTrigger.LEFT_CLICK, SingleTrigger.RIGHT_CLICK, SingleTrigger.RIGHT_CLICK),
    RRR(SingleTrigger.RIGHT_CLICK, SingleTrigger.RIGHT_CLICK, SingleTrigger.RIGHT_CLICK),
    RRL(SingleTrigger.RIGHT_CLICK, SingleTrigger.RIGHT_CLICK, SingleTrigger.LEFT_CLICK),
    RLR(SingleTrigger.RIGHT_CLICK, SingleTrigger.LEFT_CLICK, SingleTrigger.RIGHT_CLICK),
    RLL(SingleTrigger.RIGHT_CLICK, SingleTrigger.LEFT_CLICK, SingleTrigger.LEFT_CLICK),

    ;

    constructor(vararg triggers: SingleTrigger) : this(triggers.toList())

    override val id: String = "combo/${triggers.map { it.char }.joinToString("")}"

    init {
        KoishRegistries.TRIGGER.add(id, this)
    }

    companion object {
        fun fromSingleTriggers(vararg triggers: SingleTrigger): SequenceTrigger? {
            return fromSingleTriggers(triggers.toList())
        }

        fun fromSingleTriggers(triggers: List<SingleTrigger>): SequenceTrigger {
            return entries.first { it.triggers.contentEquals(triggers) }
        }

        private fun <E> List<E>.contentEquals(other: List<E>): Boolean {
            if (this.size != other.size) return false
            return this.indices.all { this[it] == other[it] }
        }
    }
}