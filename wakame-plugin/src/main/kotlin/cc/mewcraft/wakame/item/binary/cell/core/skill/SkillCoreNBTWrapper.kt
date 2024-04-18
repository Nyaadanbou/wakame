package cc.mewcraft.wakame.item.binary.cell.core.skill

import cc.mewcraft.wakame.item.CoreBinaryKeys
import cc.mewcraft.wakame.item.SkillInstance
import cc.mewcraft.wakame.item.SkillTrigger
import cc.mewcraft.wakame.util.Key
import me.lucko.helper.shadows.nbt.CompoundShadowTag
import me.lucko.helper.shadows.nbt.ShadowTag
import net.kyori.adventure.key.Key

//
// 封装类（封装 NBT 对象），本身不储存数据
//

internal class BinarySkillCoreNBTWrapper(
    private val compound: CompoundShadowTag,
) : BinarySkillCore {
    override val key: Key
        get() = compound.getIdentifier()
    override val instance: SkillInstance
        get() = SkillInstance.Noop // TODO cell-overhaul: 从 key 获取技能
    override val trigger: SkillTrigger
        get() = SkillTrigger.Noop // TODO cell-overhaul: 实现 SkillTrigger 相关的代码

    override fun asShadowTag(): ShadowTag = compound
    override fun toString(): String = compound.asString()
}

private fun CompoundShadowTag.getIdentifier(): Key {
    return Key(this.getString(CoreBinaryKeys.CORE_IDENTIFIER))
}
