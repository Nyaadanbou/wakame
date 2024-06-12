package cc.mewcraft.wakame.item.binary.cell.core.skill

import cc.mewcraft.wakame.item.CoreBinaryKeys
import cc.mewcraft.wakame.skill.SkillBinaryKeys
import cc.mewcraft.wakame.skill.trigger.Trigger
import cc.mewcraft.wakame.util.CompoundShadowTag
import me.lucko.helper.shadows.nbt.CompoundShadowTag
import me.lucko.helper.shadows.nbt.ShadowTag
import net.kyori.adventure.key.Key

//
// 数据类，本身储存数据
//

internal data class BinarySkillCoreDataHolder(
    override val key: Key,
    override val trigger: Trigger,
    override val effectiveVariant: Int
) : BinarySkillCore() {
    override fun asTag(): ShadowTag = CompoundShadowTag {
        putIdentifier(key)
        putTrigger(trigger)
        putEffectiveVariant(effectiveVariant)
    }
}

private fun CompoundShadowTag.putIdentifier(id: Key) {
    this.putString(CoreBinaryKeys.CORE_IDENTIFIER, id.asString())
}

private fun CompoundShadowTag.putTrigger(trigger: Trigger) {
    this.putString(SkillBinaryKeys.SKILL_TRIGGER, trigger.key.asString())
}

private fun CompoundShadowTag.putEffectiveVariant(variant: Int) {
    if (variant < 0)
        return
    this.putInt(SkillBinaryKeys.EFFECTIVE_VARIANT, variant)
}