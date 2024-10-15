package cc.mewcraft.wakame.compatibility.mechanic

import cc.mewcraft.wakame.attribute.Attribute
import cc.mewcraft.wakame.attribute.AttributeInstance
import cc.mewcraft.wakame.attribute.get
import io.lumine.mythic.api.adapters.AbstractEntity
import io.lumine.mythic.api.config.MythicLineConfig
import io.lumine.mythic.api.skills.INoTargetSkill
import io.lumine.mythic.api.skills.ITargetedEntitySkill
import io.lumine.mythic.api.skills.SkillMetadata
import io.lumine.mythic.api.skills.SkillResult
import io.lumine.mythic.api.skills.placeholders.PlaceholderDouble
import io.lumine.mythic.api.skills.placeholders.PlaceholderInt
import io.lumine.mythic.bukkit.utils.Schedulers
import io.lumine.mythic.core.skills.SkillExecutor
import io.lumine.mythic.core.skills.SkillMechanic
import org.bukkit.entity.Entity
import java.io.File

class AttributeMechanic(
    manager: SkillExecutor,
    file: File,
    line: String,
    mlc: MythicLineConfig,
) : SkillMechanic(manager, file, line, mlc), ITargetedEntitySkill, INoTargetSkill {
    private val attribute: Attribute = MythicMobsMechanicSupport.getAttributeFromConfig(mlc)
    private val amount: PlaceholderDouble = mlc.getPlaceholderDouble(arrayOf("amount", "amt", "a"), 0.0, *arrayOfNulls(0))
    private val duration: PlaceholderInt = mlc.getPlaceholderInteger(arrayOf("duration", "dur"), 0, *arrayOfNulls(0))

    override fun cast(data: SkillMetadata): SkillResult {
        val entity = data.caster.entity.bukkitEntity ?: return SkillResult.INVALID_TARGET
        val attributeMap = MythicMobsMechanicSupport.attributeAccessors.get<Entity>().getAttributeMap(entity)

        val attributeInstance = attributeMap.getInstance(attribute) ?: return SkillResult.INVALID_TARGET
        setBaseValueAndScheduleReset(attributeInstance, amount[data], duration[data])
        return SkillResult.SUCCESS
    }

    override fun castAtEntity(data: SkillMetadata, target: AbstractEntity): SkillResult {
        val entity = target.bukkitEntity ?: return SkillResult.INVALID_TARGET
        val attributeMap = MythicMobsMechanicSupport.attributeAccessors.get<Entity>().getAttributeMap(entity)

        val attributeInstance = attributeMap.getInstance(attribute) ?: return SkillResult.INVALID_TARGET
        setBaseValueAndScheduleReset(attributeInstance, amount[data], duration[data])
        return SkillResult.SUCCESS
    }

    private fun setBaseValueAndScheduleReset(attributeInstance: AttributeInstance, value: Double, duration: Int) {
        val original = attributeInstance.getBaseValue()
        attributeInstance.setBaseValue(value)
        if (duration > 0) {
            Schedulers.sync().runLater({
                attributeInstance.setBaseValue(original)
            }, duration.toLong())
        }
    }
}