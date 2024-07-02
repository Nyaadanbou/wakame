package cc.mewcraft.wakame.item.binary.cell.core.skill

import cc.mewcraft.nbt.CompoundTag
import cc.mewcraft.nbt.Tag
import cc.mewcraft.wakame.item.CoreBinaryKeys
import cc.mewcraft.wakame.skill.SkillBinaryKeys
import cc.mewcraft.wakame.skill.trigger.Trigger
import cc.mewcraft.wakame.skill.trigger.TriggerVariant
import cc.mewcraft.wakame.util.CompoundTag
import net.kyori.adventure.key.Key

//
// 数据类，本身储存数据
//

internal data class BinarySkillCoreDataHolder(
    override val key: Key,
    override val trigger: Trigger,
    override val variant: TriggerVariant,
) : BinarySkillCore() {
    override fun asTag(): Tag = CompoundTag {
        putIdentifier(key)
        putTrigger(trigger)
        putEffectiveVariant(variant)
    }
}

private fun CompoundTag.putIdentifier(id: Key) {
    this.putString(CoreBinaryKeys.CORE_IDENTIFIER, id.asString())
}

private fun CompoundTag.putTrigger(trigger: Trigger) {
    this.putString(SkillBinaryKeys.SKILL_TRIGGER, trigger.key.asString())
}

private fun CompoundTag.putEffectiveVariant(variant: TriggerVariant) {
    if (variant == TriggerVariant.any())
        return
    this.putInt(SkillBinaryKeys.EFFECTIVE_VARIANT, variant.id)
}