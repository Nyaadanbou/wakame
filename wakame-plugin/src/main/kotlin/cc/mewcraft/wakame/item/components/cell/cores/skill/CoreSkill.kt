package cc.mewcraft.wakame.item.components.cell.cores.skill

import cc.mewcraft.nbt.CompoundTag
import cc.mewcraft.nbt.Tag
import cc.mewcraft.wakame.display.LoreLine
import cc.mewcraft.wakame.item.CoreBinaryKeys
import cc.mewcraft.wakame.item.components.cell.Core
import cc.mewcraft.wakame.item.components.cell.CoreType
import cc.mewcraft.wakame.registry.SkillRegistry
import cc.mewcraft.wakame.skill.Skill
import cc.mewcraft.wakame.skill.SkillBinaryKeys
import cc.mewcraft.wakame.skill.trigger.ConfiguredSkill
import cc.mewcraft.wakame.skill.trigger.SingleTrigger
import cc.mewcraft.wakame.skill.trigger.Trigger
import cc.mewcraft.wakame.util.CompoundTag
import cc.mewcraft.wakame.util.Key
import cc.mewcraft.wakame.util.getIntOrNull
import cc.mewcraft.wakame.util.getStringOrNull
import cc.mewcraft.wakame.util.krequire
import cc.mewcraft.wakame.util.toSimpleString
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import net.kyori.adventure.key.Key
import net.kyori.examination.ExaminableProperty
import org.spongepowered.configurate.ConfigurationNode
import java.util.stream.Stream

/**
 * 构建一个 [CoreSkill].
 */
fun CoreSkill(nbt: CompoundTag): CoreSkill {
    return CoreSkill(
        key = nbt.getIdentifier(),
        trigger = nbt.getTrigger(),
        variant = nbt.getVariant()
    )
}

/**
 * 构建一个 [CoreSkill].
 */
fun CoreSkill(node: ConfigurationNode): CoreSkill {
    val configuredSkill = node.krequire<ConfiguredSkill>()
    val key = configuredSkill.key
    val trigger = configuredSkill.trigger
    val variant = configuredSkill.variant
    return CoreSkill(key, trigger, variant)
}

data class CoreSkill(
    override val key: Key,
    val trigger: Trigger,
    val variant: ConfiguredSkill.Variant, // TODO 这个类叫做 TriggerVariant 怎么样?
) : Core {
    val instance: Skill
        get() = SkillRegistry.TYPES[key]

    override val type: CoreType<*> = Type
    override val isNoop: Boolean = false
    override val isEmpty: Boolean = false

    override fun provideDisplayLore(): LoreLine {
        val tooltipKey = SkillDisplaySupport.getLineKey(this) ?: return LoreLine.noop()
        val tooltipText = instance.displays.tooltips
        val resolver = instance.conditions.resolver
        val lineText = tooltipText.mapTo(ObjectArrayList(tooltipText.size)) { SkillDisplaySupport.mini().deserialize(it, resolver) }
        return LoreLine.simple(tooltipKey, lineText)
    }

    override fun asTag(): Tag = CompoundTag {
        putIdentifier(key)
        putTrigger(trigger)
        putVariant(variant)
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> {
        return Stream.of(ExaminableProperty.of("key", key))
    }

    override fun toString(): String {
        return toSimpleString()
    }

    companion object Type : CoreType<CoreSkill>
}


//<editor-fold desc="Convenient extension functions">
private fun CompoundTag.getIdentifier(): Key {
    return Key(this.getString(CoreBinaryKeys.CORE_IDENTIFIER))
}

private fun CompoundTag.getTrigger(): Trigger {
    return this.getStringOrNull(SkillBinaryKeys.SKILL_TRIGGER)?.let { SkillRegistry.TRIGGERS[Key(it)] } ?: SingleTrigger.NOOP
}

private fun CompoundTag.getVariant(): ConfiguredSkill.Variant {
    val variant = this.getIntOrNull(SkillBinaryKeys.EFFECTIVE_VARIANT) ?: return ConfiguredSkill.Variant.any()
    return ConfiguredSkill.Variant.of(variant)
}

private fun CompoundTag.putIdentifier(id: Key) {
    this.putString(CoreBinaryKeys.CORE_IDENTIFIER, id.asString())
}

private fun CompoundTag.putTrigger(trigger: Trigger) {
    this.putString(SkillBinaryKeys.SKILL_TRIGGER, trigger.key.asString())
}

private fun CompoundTag.putVariant(variant: ConfiguredSkill.Variant) {
    if (variant == ConfiguredSkill.Variant.any())
        return
    this.putInt(SkillBinaryKeys.EFFECTIVE_VARIANT, variant.variant)
}
//</editor-fold>