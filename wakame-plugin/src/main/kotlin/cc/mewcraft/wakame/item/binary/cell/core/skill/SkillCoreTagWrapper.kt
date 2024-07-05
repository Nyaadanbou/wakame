package cc.mewcraft.wakame.item.binary.cell.core.skill

import cc.mewcraft.nbt.CompoundTag
import cc.mewcraft.nbt.Tag
import cc.mewcraft.wakame.item.CoreBinaryKeys
import cc.mewcraft.wakame.registry.SkillRegistry
import cc.mewcraft.wakame.skill.SkillBinaryKeys
import cc.mewcraft.wakame.skill.trigger.SingleTrigger
import cc.mewcraft.wakame.skill.trigger.Trigger
import cc.mewcraft.wakame.skill.trigger.TriggerVariant
import cc.mewcraft.wakame.util.Key
import cc.mewcraft.wakame.util.getIntOrNull
import cc.mewcraft.wakame.util.getStringOrNull
import net.kyori.adventure.key.Key

//
// 封装类（封装 NBT 对象），本身不储存数据
//

internal class BinarySkillCoreTagWrapper(
    private val compound: CompoundTag,
) : BinarySkillCore() {
    override val key: Key
        get() = compound.getIdentifier()
    override val trigger: Trigger
        get() = compound.getTrigger()
    override val variant: TriggerVariant
        get() = compound.getEffectiveVariant()

    override fun clear() {
        compound.tags().clear()
    }

    override fun serializeAsTag(): Tag {
        return compound
    }

    override fun toString(): String {
        return compound.asString()
    }
}

private fun CompoundTag.getIdentifier(): Key {
    return Key(this.getString(CoreBinaryKeys.CORE_IDENTIFIER))
}

private fun CompoundTag.getTrigger(): Trigger {
    return this.getStringOrNull(SkillBinaryKeys.SKILL_TRIGGER)?.let { SkillRegistry.TRIGGERS[Key(it)] } ?: SingleTrigger.NOOP
}

private fun CompoundTag.getEffectiveVariant(): TriggerVariant {
    val variant = this.getIntOrNull(SkillBinaryKeys.EFFECTIVE_VARIANT) ?: return TriggerVariant.any()
    return TriggerVariant.of(variant)
}