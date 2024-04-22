package cc.mewcraft.wakame.item.binary.cell.core.skill

import cc.mewcraft.wakame.item.CoreBinaryKeys
import cc.mewcraft.wakame.item.SkillInstance
import cc.mewcraft.wakame.skill.SkillTrigger
import cc.mewcraft.wakame.util.CompoundShadowTag
import me.lucko.helper.shadows.nbt.CompoundShadowTag
import me.lucko.helper.shadows.nbt.ShadowTag
import net.kyori.adventure.key.Key

//
// 数据类，本身储存数据
//

internal data class BinarySkillCoreDataHolder(
    override val key: Key,
    override val trigger: SkillTrigger,
) : BinarySkillCore {
    override val instance: SkillInstance
        get() = SkillInstance.Noop // TODO cell-overhaul: 从 key 获取技能

    override fun asShadowTag(): ShadowTag = CompoundShadowTag {
        putIdentifier(key)
        // TODO 也将 trigger 写入 NBT
    }
}

private fun CompoundShadowTag.putIdentifier(id: Key) {
    this.putString(CoreBinaryKeys.CORE_IDENTIFIER, id.asString())
}
