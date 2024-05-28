package cc.mewcraft.wakame.item.binary.cell.core.skill

import cc.mewcraft.wakame.display.LoreLine
import cc.mewcraft.wakame.display.NoopLoreLine
import cc.mewcraft.wakame.item.binary.cell.core.BinaryCore
import cc.mewcraft.wakame.registry.SkillRegistry
import cc.mewcraft.wakame.skill.Skill
import cc.mewcraft.wakame.skill.SkillTrigger
import cc.mewcraft.wakame.util.toSimpleString
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import net.kyori.examination.ExaminableProperty
import java.util.stream.Stream

sealed class BinarySkillCore : BinaryCore {
    val instance: Skill
        get() = SkillRegistry.INSTANCE[key]

    abstract val trigger: SkillTrigger

    override fun provideDisplayLore(): LoreLine {
        val lineKey = SkillDisplaySupport.getLineKey(this) ?: return NoopLoreLine
        val display = instance.displays
        val tooltips = display.tooltips
        val tagResolvers = instance.conditions.tagResolvers
        val lineText = tooltips.mapTo(ObjectArrayList(tooltips.size)) { SkillDisplaySupport.mini().deserialize(it, *tagResolvers) }
        return SkillLoreLine(lineKey, lineText)
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> {
        return Stream.of(ExaminableProperty.of("key", key))
    }

    override fun toString(): String {
        return toSimpleString()
    }
}
