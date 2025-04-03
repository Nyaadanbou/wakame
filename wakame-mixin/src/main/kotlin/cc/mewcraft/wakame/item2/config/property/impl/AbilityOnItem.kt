package cc.mewcraft.wakame.item2.config.property.impl

import cc.mewcraft.wakame.ability2.meta.AbilityMeta
import cc.mewcraft.wakame.ability2.trigger.AbilityTrigger
import cc.mewcraft.wakame.ability2.trigger.AbilityTriggerVariant
import cc.mewcraft.wakame.molang.Expression
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Setting

/**
 * 首先请参考 [cc.mewcraft.wakame.ability2.meta.AbilityMetaType].
 *
 * 这个类表示已经填充了参数的技能类型, 附加了额外的信息用于实现玩家释放技能的逻辑:
 *  - [trigger] : 释放技能的触发器.
 *  - [variant] : 释放技能的触发器变体.
 *  - [manaCost] : 释放技能的魔法消耗.
 */
@ConfigSerializable
data class AbilityOnItem(
    @Setting("id")
    val meta: AbilityMeta,
    val trigger: AbilityTrigger?,
    val variant: AbilityTriggerVariant = AbilityTriggerVariant.Companion.any(),
    val manaCost: Expression?,
)