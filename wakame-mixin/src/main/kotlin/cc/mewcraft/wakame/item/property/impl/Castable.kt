package cc.mewcraft.wakame.item.property.impl

import cc.mewcraft.wakame.ability.trigger.AbilityTrigger
import cc.mewcraft.wakame.integration.skill.SkillWrapper
import cc.mewcraft.wakame.item.property.impl.GenericCastableTrigger.LEFT_CLICK
import cc.mewcraft.wakame.item.property.impl.GenericCastableTrigger.RIGHT_CLICK
import cc.mewcraft.wakame.registry.BuiltInRegistries
import cc.mewcraft.wakame.registry.entry.RegistryEntry
import net.kyori.adventure.key.Key
import org.bukkit.Input
import org.spongepowered.configurate.objectmapping.ConfigSerializable

/**
 * 机制, 储存了一个物品可以释放出的机制的信息.
 */
@ConfigSerializable
data class Castable(
    /**
     * 该机制施放的技能.
     */
    val skill: SkillWrapper,
    /**
     * 触发该机制的触发器.
     */
    val trigger: RegistryEntry<CastableTrigger>,
    /**
     * 触发该机制所需要的魔法值.
     */
    val manaCost: Double = .0,
)

//<editor-fold desc="CastableTrigger">

/**
 * 机制触发器.
 */
interface CastableTrigger {
    /**
     * 用来在配置文件里指定该 [AbilityTrigger].
     */
    val id: Key
}

/**
 * 单击左/右键.
 */
enum class GenericCastableTrigger(
    /**
     * 用于拼接成 [SequenceCastableTrigger] 的字符.
     */
    val char: Char,
    /**
     * 用来在配置文件里指定该 [AbilityTrigger].
     */
    override val id: Key,
) : CastableTrigger {

    /**
     * 玩家左键点击时触发.
     */
    LEFT_CLICK('0', "left_click"),
    /**
     * 玩家右键点击时触发.
     */
    RIGHT_CLICK('1', "right_click"),
    ;

    init {
        BuiltInRegistries.CASTABLE_TRIGGER.add(id, this)
    }

    constructor(char: Char, id: String) : this(char, Key.key("koish", "generic/${id}"))
}

/**
 * 连续点击左/右键组成的特定序列.
 */
enum class SequenceCastableTrigger(
    /**
     * 组成该组合键序列的 [GenericCastableTrigger].
     */
    val sequence: Array<GenericCastableTrigger>,
) : CastableTrigger {

    LLL(arrayOf(LEFT_CLICK, LEFT_CLICK, LEFT_CLICK)),
    LLR(arrayOf(LEFT_CLICK, LEFT_CLICK, RIGHT_CLICK)),
    LRL(arrayOf(LEFT_CLICK, RIGHT_CLICK, LEFT_CLICK)),
    LRR(arrayOf(LEFT_CLICK, RIGHT_CLICK, RIGHT_CLICK)),
    RLL(arrayOf(RIGHT_CLICK, LEFT_CLICK, LEFT_CLICK)),
    RLR(arrayOf(RIGHT_CLICK, LEFT_CLICK, RIGHT_CLICK)),
    RRL(arrayOf(RIGHT_CLICK, RIGHT_CLICK, LEFT_CLICK)),
    RRR(arrayOf(RIGHT_CLICK, RIGHT_CLICK, RIGHT_CLICK)),
    ;

    /**
     * 用来在配置文件里指定该 [AbilityTrigger].
     */
    override val id: Key = Key.key("koish", "sequence/${sequence.map(GenericCastableTrigger::char).joinToString("")}")

    init {
        BuiltInRegistries.CASTABLE_TRIGGER.add(id, this)
    }
}

/**
 * 特定动作.
 */
enum class SpecialCastableTrigger(
    override val id: Key,
) : CastableTrigger {

    /**
     * 在生效槽位装备该物品时触发一次.
     */
    ON_EQUIP("on_equip"),
    /**
     * 从生效槽位脱下该物品时触发一次.
     */
    ON_UNEQUIP("on_unequip")
    ;

    constructor(id: String) : this(Key.key("koish", "special/${id}"))

    init {
        BuiltInRegistries.CASTABLE_TRIGGER.add(id, this)
    }
}

/**
 * 输入.
 */
enum class InputCastableTrigger(
    override val id: Key,
) : CastableTrigger {
    FORWARD("forward"),
    BACKWARD("backward"),
    LEFT("left"),
    RIGHT("right"),
    JUMP("jump"),
    SNEAK("sneak"),
    SPRINT("sprint")
    ;

    constructor(id: String) : this(Key.key("koish", "input/${id}"))

    init {
        BuiltInRegistries.CASTABLE_TRIGGER.add(id, this)
    }

    fun equals(input: Input): Boolean {
        return when {
            input.isForward && this == FORWARD -> true
            input.isBackward && this == BACKWARD -> true
            input.isLeft && this == LEFT -> true
            input.isRight && this == RIGHT -> true
            input.isJump && this == JUMP -> true
            input.isSneak && this == SNEAK -> true
            input.isSprint && this == SPRINT -> true
            else -> false
        }
    }
}

//</editor-fold>