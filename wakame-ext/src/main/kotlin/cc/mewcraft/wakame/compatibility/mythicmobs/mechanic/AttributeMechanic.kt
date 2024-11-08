package cc.mewcraft.wakame.compatibility.mythicmobs.mechanic

import cc.mewcraft.wakame.attribute.*
import io.lumine.mythic.api.adapters.AbstractEntity
import io.lumine.mythic.api.config.MythicLineConfig
import io.lumine.mythic.api.skills.*
import io.lumine.mythic.api.skills.placeholders.PlaceholderDouble
import io.lumine.mythic.api.skills.placeholders.PlaceholderInt
import io.lumine.mythic.bukkit.utils.Schedulers
import io.lumine.mythic.core.skills.SkillExecutor
import io.lumine.mythic.core.skills.SkillMechanic
import org.bukkit.entity.LivingEntity
import org.koin.core.component.*
import java.io.File

class AttributeMechanic(
    manager: SkillExecutor,
    file: File,
    line: String,
    mlc: MythicLineConfig,
) : SkillMechanic(manager, file, line, mlc), ITargetedEntitySkill, INoTargetSkill, KoinComponent {

    init {
        threadSafetyLevel = ThreadSafetyLevel.SYNC_ONLY
    }

    private val attributeMapAccess: AttributeMapAccess<Any> by inject()

    private val amount: PlaceholderDouble = mlc.getPlaceholderDouble(arrayOf("amount", "amt", "a"), 0.0, *arrayOfNulls(0))
    private val duration: PlaceholderInt = mlc.getPlaceholderInteger(arrayOf("duration", "dur"), 0, *arrayOfNulls(0))
    private val attribute: Attribute = mlc.getString(arrayOf("attribute", "attr"))
        ?.let { parsed -> get<AttributeProvider>().getSingleton(parsed) }
        ?: throw IllegalArgumentException("Invalid attribute from line: $line")

    override fun cast(data: SkillMetadata): SkillResult {
        val targetEntity = data.caster?.entity?.bukkitEntity as? LivingEntity ?: return SkillResult.INVALID_TARGET
        val attributeMap = attributeMapAccess.get(targetEntity).getOrNull() ?: return SkillResult.ERROR
        val attributeInstance = attributeMap.getInstance(attribute) ?: return SkillResult.ERROR
        setBaseValueAndScheduleReset(attributeInstance, amount[data], duration[data])
        return SkillResult.SUCCESS
    }

    override fun castAtEntity(data: SkillMetadata, target: AbstractEntity): SkillResult {
        val targetEntity = target.bukkitEntity as? LivingEntity ?: return SkillResult.INVALID_TARGET
        val attributeMap = attributeMapAccess.get(targetEntity).getOrNull() ?: return SkillResult.ERROR
        val attributeInstance = attributeMap.getInstance(attribute) ?: return SkillResult.ERROR
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