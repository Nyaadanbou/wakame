package cc.mewcraft.wakame.ability2.trigger

import cc.mewcraft.wakame.registry2.KoishRegistries2
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
enum class AbilitySingleTrigger(
    /**
     * 用于拼接成 [AbilitySequenceTrigger] 的字符.
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
        KoishRegistries2.ABILITY_TRIGGER.add(id, this)
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
 * 代表一个由多个按键输入组成的操作, 也就是该触发器要求玩家按顺序触发指定的 [AbilitySingleTrigger].
 */
enum class AbilitySequenceTrigger(
    /**
     * 组成该触发器序列的触发器 (按顺序).
     */
    val triggers: List<AbilitySingleTrigger>,
) : AbilityTrigger {

    LLL(AbilitySingleTrigger.LEFT_CLICK, AbilitySingleTrigger.LEFT_CLICK, AbilitySingleTrigger.LEFT_CLICK),
    LLR(AbilitySingleTrigger.LEFT_CLICK, AbilitySingleTrigger.LEFT_CLICK, AbilitySingleTrigger.RIGHT_CLICK),
    LRL(AbilitySingleTrigger.LEFT_CLICK, AbilitySingleTrigger.RIGHT_CLICK, AbilitySingleTrigger.LEFT_CLICK),
    LRR(AbilitySingleTrigger.LEFT_CLICK, AbilitySingleTrigger.RIGHT_CLICK, AbilitySingleTrigger.RIGHT_CLICK),
    RRR(AbilitySingleTrigger.RIGHT_CLICK, AbilitySingleTrigger.RIGHT_CLICK, AbilitySingleTrigger.RIGHT_CLICK),
    RRL(AbilitySingleTrigger.RIGHT_CLICK, AbilitySingleTrigger.RIGHT_CLICK, AbilitySingleTrigger.LEFT_CLICK),
    RLR(AbilitySingleTrigger.RIGHT_CLICK, AbilitySingleTrigger.LEFT_CLICK, AbilitySingleTrigger.RIGHT_CLICK),
    RLL(AbilitySingleTrigger.RIGHT_CLICK, AbilitySingleTrigger.LEFT_CLICK, AbilitySingleTrigger.LEFT_CLICK),
    ;

    constructor(vararg triggers: AbilitySingleTrigger) : this(triggers.toList())

    override val id: String = "combo/${triggers.map { it.char }.joinToString("")}"

    init {
        KoishRegistries2.ABILITY_TRIGGER.add(id, this)
    }

    companion object {
        fun of(vararg triggers: AbilitySingleTrigger): AbilitySequenceTrigger? {
            return of(triggers.toList())
        }

        fun of(triggers: List<AbilitySingleTrigger>): AbilitySequenceTrigger {
            return entries.first { it.triggers.contentEquals(triggers) }
        }

        private fun <E> List<E>.contentEquals(other: List<E>): Boolean {
            if (this.size != other.size) return false
            return this.indices.all { this[it] == other[it] }
        }
    }
}