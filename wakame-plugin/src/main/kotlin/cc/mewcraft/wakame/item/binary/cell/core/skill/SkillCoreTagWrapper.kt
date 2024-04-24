package cc.mewcraft.wakame.item.binary.cell.core.skill

import cc.mewcraft.wakame.item.CoreBinaryKeys
import cc.mewcraft.wakame.skill.SkillBinaryKeys
import cc.mewcraft.wakame.skill.SkillTrigger
import cc.mewcraft.wakame.util.Key
import cc.mewcraft.wakame.util.getStringOrNull
import me.lucko.helper.shadows.nbt.CompoundShadowTag
import me.lucko.helper.shadows.nbt.ShadowTag
import net.kyori.adventure.key.Key

//
// 封装类（封装 NBT 对象），本身不储存数据
//

internal class BinarySkillCoreTagWrapper(
    private val compound: CompoundShadowTag,
) : BinarySkillCore() {
    override val key: Key
        get() = compound.getIdentifier()
    override val instance: Key = key
    override val trigger: SkillTrigger
        get() = compound.getTrigger()

    override fun clear() {
        compound.tags().clear()
    }

    override fun asTag(): ShadowTag {
        return compound
    }

    override fun toString(): String {
        return compound.asString()
    }
}

private fun CompoundShadowTag.getIdentifier(): Key {
    return Key(this.getString(CoreBinaryKeys.CORE_IDENTIFIER))
}

private fun CompoundShadowTag.getTrigger(): SkillTrigger {
    return this.getStringOrNull(SkillBinaryKeys.SKILL_TRIGGER)?.let { SkillTrigger.fromStringOrNull(it) } ?: SkillTrigger.Noop
}