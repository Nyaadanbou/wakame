package cc.mewcraft.wakame.item.binary.cell.core.skill

import cc.mewcraft.wakame.item.CoreBinaryKeys
import cc.mewcraft.wakame.registry.SkillRegistry
import cc.mewcraft.wakame.skill.ConfiguredSkill
import cc.mewcraft.wakame.skill.SkillBinaryKeys
import cc.mewcraft.wakame.skill.SkillTrigger
import cc.mewcraft.wakame.util.CompoundShadowTag
import me.lucko.helper.shadows.nbt.CompoundShadowTag
import me.lucko.helper.shadows.nbt.ShadowTag
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver

//
// 数据类，本身储存数据
//

internal data class BinarySkillCoreDataHolder(
    override val key: Key,
    override val trigger: SkillTrigger,
) : BinarySkillCore() {
    override val instance: ConfiguredSkill
        get() = SkillRegistry.INSTANCE[key]

    override fun asTag(): ShadowTag = CompoundShadowTag {
        putIdentifier(key)
        putTrigger(trigger)
    }

    override fun provideTagResolverForPlay(): TagResolver {
        TODO("Not yet implemented")
    }

    override fun provideTagResolverForShow(): TagResolver {
        TODO("Not yet implemented")
    }

}

private fun CompoundShadowTag.putIdentifier(id: Key) {
    this.putString(CoreBinaryKeys.CORE_IDENTIFIER, id.asString())
}

private fun CompoundShadowTag.putTrigger(trigger: SkillTrigger) {
    this.putString(SkillBinaryKeys.SKILL_TRIGGER, trigger.key.asString())
}
