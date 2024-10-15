package cc.mewcraft.wakame.compatibility.mechanic

import cc.mewcraft.wakame.attribute.Attribute
import cc.mewcraft.wakame.attribute.AttributeInstance
import cc.mewcraft.wakame.attribute.AttributeModifier
import cc.mewcraft.wakame.attribute.get
import cc.mewcraft.wakame.util.Key
import io.lumine.mythic.api.adapters.AbstractEntity
import io.lumine.mythic.api.config.MythicLineConfig
import io.lumine.mythic.api.skills.INoTargetSkill
import io.lumine.mythic.api.skills.ITargetedEntitySkill
import io.lumine.mythic.api.skills.SkillMetadata
import io.lumine.mythic.api.skills.SkillResult
import io.lumine.mythic.api.skills.placeholders.PlaceholderDouble
import io.lumine.mythic.api.skills.placeholders.PlaceholderInt
import io.lumine.mythic.api.skills.placeholders.PlaceholderString
import io.lumine.mythic.bukkit.utils.Schedulers
import io.lumine.mythic.core.skills.SkillExecutor
import io.lumine.mythic.core.skills.SkillMechanic
import org.bukkit.entity.Entity
import java.io.File

class AttributeModifierMechanic(
    manager: SkillExecutor,
    file: File,
    line: String,
    mlc: MythicLineConfig,
) : SkillMechanic(manager, file, line, mlc), ITargetedEntitySkill, INoTargetSkill {
    private val attribute: Attribute = MythicMobsMechanicSupport.getAttributeFromConfig(mlc)

    private val operation: AttributeModifier.Operation = mlc.getString(arrayOf("operation", "op"), "add").let { AttributeModifier.Operation.byKeyOrThrow(it) }
    private val name: PlaceholderString = mlc.getPlaceholderString(arrayOf("name", "modifierName"), null, *arrayOfNulls(0)) ?: throw IllegalArgumentException("name is required")
    private val amount: PlaceholderDouble = mlc.getPlaceholderDouble(arrayOf("amount", "amt", "a"), 0.0, *arrayOfNulls(0))
    private val duration: PlaceholderInt = mlc.getPlaceholderInteger(arrayOf("duration", "dur"), 0, *arrayOfNulls(0))

    override fun cast(data: SkillMetadata): SkillResult {
        val entity = data.caster.entity.bukkitEntity ?: return SkillResult.INVALID_TARGET
        val attributeMap = MythicMobsMechanicSupport.attributeAccessors.get<Entity>().getAttributeMap(entity)

        val modifier = AttributeModifier(Key(name[data]), amount[data], this.operation)
        val attributeInstance = attributeMap.getInstance(attribute) ?: return SkillResult.INVALID_TARGET
        addModifierAndScheduleRemoval(attributeInstance, modifier, duration[data])

        return SkillResult.SUCCESS
    }

    override fun castAtEntity(data: SkillMetadata, target: AbstractEntity): SkillResult {
        val entity = target.bukkitEntity
        val attributeMap = MythicMobsMechanicSupport.attributeAccessors.get<Entity>().getAttributeMap(entity)

        val modifier = AttributeModifier(Key(name[data]), amount[data], this.operation)
        val attributeInstance = attributeMap.getInstance(attribute) ?: return SkillResult.INVALID_TARGET
        addModifierAndScheduleRemoval(attributeInstance, modifier, duration[data])

        return SkillResult.SUCCESS
    }

    private fun addModifierAndScheduleRemoval(attributeInstance: AttributeInstance, modifier: AttributeModifier, duration: Int) {
        attributeInstance.addModifier(modifier)
        if (duration > 0) {
            Schedulers.sync().runLater({
                attributeInstance.removeModifier(modifier)
            }, duration.toLong())
        }
    }
}