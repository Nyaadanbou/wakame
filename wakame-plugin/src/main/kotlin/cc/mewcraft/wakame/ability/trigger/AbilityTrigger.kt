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
sealed interface AbilityTrigger : Examinable {
    /**
     * 用来在配置文件里指定该 [AbilityTrigger].
     */
    val id: String
}

/**
 * 代表一个由单个按键输入完成的操作.
 */
enum class SingleAbilityTrigger(
    /**
     * 用于拼接成 [SequenceAbilityTrigger] 的字符.
     */
    val char: Char,
    override val id: String,
) : AbilityTrigger {
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
    ;

    init {
        KoishRegistries.ABILITY_TRIGGER.add(id, this)
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
 * 代表一个由多个按键输入组成的操作, 也就是该触发器要求玩家按顺序触发指定的 [SingleAbilityTrigger].
 */
enum class SequenceAbilityTrigger(
    /**
     * 组成该触发器序列的触发器 (按顺序).
     */
    val triggers: List<SingleAbilityTrigger>,
) : AbilityTrigger {

    LLL(SingleAbilityTrigger.LEFT_CLICK, SingleAbilityTrigger.LEFT_CLICK, SingleAbilityTrigger.LEFT_CLICK),
    LLR(SingleAbilityTrigger.LEFT_CLICK, SingleAbilityTrigger.LEFT_CLICK, SingleAbilityTrigger.RIGHT_CLICK),
    LRL(SingleAbilityTrigger.LEFT_CLICK, SingleAbilityTrigger.RIGHT_CLICK, SingleAbilityTrigger.LEFT_CLICK),
    LRR(SingleAbilityTrigger.LEFT_CLICK, SingleAbilityTrigger.RIGHT_CLICK, SingleAbilityTrigger.RIGHT_CLICK),
    RRR(SingleAbilityTrigger.RIGHT_CLICK, SingleAbilityTrigger.RIGHT_CLICK, SingleAbilityTrigger.RIGHT_CLICK),
    RRL(SingleAbilityTrigger.RIGHT_CLICK, SingleAbilityTrigger.RIGHT_CLICK, SingleAbilityTrigger.LEFT_CLICK),
    RLR(SingleAbilityTrigger.RIGHT_CLICK, SingleAbilityTrigger.LEFT_CLICK, SingleAbilityTrigger.RIGHT_CLICK),
    RLL(SingleAbilityTrigger.RIGHT_CLICK, SingleAbilityTrigger.LEFT_CLICK, SingleAbilityTrigger.LEFT_CLICK),
    ;

    constructor(vararg triggers: SingleAbilityTrigger) : this(triggers.toList())

    override val id: String = "combo/${triggers.map { it.char }.joinToString("")}"

    init {
        KoishRegistries.ABILITY_TRIGGER.add(id, this)
    }

    companion object {
        fun of(vararg triggers: SingleAbilityTrigger): SequenceAbilityTrigger? {
            return of(triggers.toList())
        }

        fun of(triggers: List<SingleAbilityTrigger>): SequenceAbilityTrigger {
            return entries.first { it.triggers.contentEquals(triggers) }
        }

        private fun <E> List<E>.contentEquals(other: List<E>): Boolean {
            if (this.size != other.size) return false
            return this.indices.all { this[it] == other[it] }
        }
    }
}