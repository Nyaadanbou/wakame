package cc.mewcraft.wakame.item.binary.cell.core.skill

import cc.mewcraft.wakame.item.binary.cell.core.BinaryCore
import cc.mewcraft.wakame.skill.ConfiguredSkill
import cc.mewcraft.wakame.skill.SkillTrigger
import cc.mewcraft.wakame.util.toSimpleString
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import net.kyori.examination.ExaminableProperty
import java.util.stream.Stream

sealed class BinarySkillCore : BinaryCore {
    abstract val instance: ConfiguredSkill
    abstract val trigger: SkillTrigger

    override fun provideTagResolverForPlay(): TagResolver {
        TODO("Not yet implemented")
    }

    override fun provideTagResolverForShow(): TagResolver {
        TODO("Not yet implemented")
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> {
        return Stream.of(ExaminableProperty.of("key", instance.key))
    }

    override fun toString(): String {
        return toSimpleString()
    }
}
