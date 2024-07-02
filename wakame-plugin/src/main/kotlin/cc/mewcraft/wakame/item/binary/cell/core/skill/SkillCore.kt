package cc.mewcraft.wakame.item.binary.cell.core.skill

import cc.mewcraft.wakame.display.LoreLine
import cc.mewcraft.wakame.item.binary.cell.core.BinaryCore
import cc.mewcraft.wakame.registry.SkillRegistry
import cc.mewcraft.wakame.skill.Skill
import cc.mewcraft.wakame.skill.trigger.Trigger
import cc.mewcraft.wakame.skill.trigger.TriggerVariant
import cc.mewcraft.wakame.util.toSimpleString
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import net.kyori.examination.ExaminableProperty
import java.util.stream.Stream

sealed class BinarySkillCore : BinaryCore {
    val instance: Skill
        get() = SkillRegistry.TYPES[key]

    abstract val trigger: Trigger
    abstract val variant: TriggerVariant

    override fun provideDisplayLore(): LoreLine {
        val lineKey = SkillDisplaySupport.getLineKey(this) ?: return LoreLine.noop()
        val display = instance.displays
        val tooltips = display.tooltips
        val tagResolvers = instance.conditions.resolver
        val lineText = tooltips.mapTo(ObjectArrayList(tooltips.size)) { SkillDisplaySupport.mini().deserialize(it, tagResolvers) }
        return LoreLine.simple(lineKey, lineText)
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> {
        return Stream.of(ExaminableProperty.of("key", key))
    }

    override fun toString(): String {
        return toSimpleString()
    }
}
