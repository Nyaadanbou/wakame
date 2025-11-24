package cc.mewcraft.wakame.item.property.impl

import cc.mewcraft.wakame.ability.trigger.AbilityTrigger
import cc.mewcraft.wakame.integration.skill.SkillWrapper
import cc.mewcraft.wakame.item.property.impl.GenericCastableTrigger.LEFT_CLICK
import cc.mewcraft.wakame.item.property.impl.GenericCastableTrigger.RIGHT_CLICK
import cc.mewcraft.wakame.registry.BuiltInRegistries
import cc.mewcraft.wakame.registry.entry.RegistryEntry
import net.kyori.adventure.key.Key
import org.spongepowered.configurate.objectmapping.ConfigSerializable

/**
 * 机制, 储存了一个物品可以释放出的机制的信息.
 */
@ConfigSerializable
data class Castable(
    /**
     * 该机制释放的技能.
     */
    val skill: SkillWrapper,
    /**
     * 触发该机制的触发器.
     */
    val trigger: RegistryEntry<CastableTrigger>,
    /**
     * 触发该机制所需要的魔法值.
     */
    val manaCost: Double,
)

interface CastableTrigger {
    /**
     * 用来在配置文件里指定该 [AbilityTrigger].
     */
    val id: Key
}

enum class GenericCastableTrigger(
    /**
     * 用于拼接成 [ComboCastableTrigger] 的字符.
     */
    val char: Char,
    /**
     * 用来在配置文件里指定该 [AbilityTrigger].
     */
    override val id: Key,
) : CastableTrigger {

    LEFT_CLICK('0', "generic/left_click"),
    RIGHT_CLICK('1', "generic/right_click"),
    ;

    init {
        BuiltInRegistries.CASTABLE_TRIGGER.add(id, this)
    }

    constructor(char: Char, id: String) : this(char, Key.key("koish", id))
}

enum class ComboCastableTrigger(
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
    override val id: Key = Key.key("koish", "combo/${sequence.map(GenericCastableTrigger::char).joinToString("")}")

    init {
        BuiltInRegistries.CASTABLE_TRIGGER.add(id, this)
    }
}