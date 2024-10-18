package cc.mewcraft.wakame.compatibility.mythicmobs

import cc.mewcraft.wakame.attribute.*
import io.lumine.mythic.api.adapters.AbstractEntity
import io.lumine.mythic.api.config.MythicLineConfig
import io.lumine.mythic.api.skills.*
import io.lumine.mythic.api.skills.placeholders.*
import io.lumine.mythic.bukkit.utils.Schedulers
import io.lumine.mythic.core.skills.SkillExecutor
import io.lumine.mythic.core.skills.SkillMechanic
import net.kyori.adventure.key.Key
import org.bukkit.entity.LivingEntity
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import java.io.File

class AttributeModifierMechanic(
    manager: SkillExecutor,
    file: File,
    line: String,
    mlc: MythicLineConfig,
) : SkillMechanic(manager, file, line, mlc), ITargetedEntitySkill, INoTargetSkill, KoinComponent {
    private val attributeProvider: AttributeProvider by inject()
    private val attributeMapAccess: AttributeMapAccess<LivingEntity> by inject(named(AttributeMapAccess.FOR_ENTITY))

    private val attribute: Attribute = mlc.getString(arrayOf("attribute", "attr"))
        ?.let { parsed -> attributeProvider.getBy(parsed) }
        ?: throw IllegalArgumentException("Invalid attribute from line: $line")
    private val name: PlaceholderString = mlc.getPlaceholderString(arrayOf("name"), null, *emptyArray())
        ?: throw IllegalArgumentException("Invalid attribute modifier name from line: $line")
    private val amount: PlaceholderDouble = mlc.getPlaceholderDouble(arrayOf("amount"), .0, *emptyArray())
    private val operation: AttributeModifier.Operation = mlc.getEnum(arrayOf("operation", "op"), AttributeModifier.Operation::class.java, AttributeModifier.Operation.ADD)
    private val duration: PlaceholderInt = mlc.getPlaceholderInteger(arrayOf("duration"), 0, *emptyArray())

    override fun cast(data: SkillMetadata): SkillResult {
        val targetEntity = data.caster.entity.bukkitEntity as? LivingEntity ?: return SkillResult.INVALID_TARGET
        val attributeMap = attributeMapAccess.get(targetEntity)
        val modifier = AttributeModifier(Key.key(name[data]), amount[data], operation)
        val attributeInstance = attributeMap.getInstance(attribute) ?: return SkillResult.INVALID_TARGET
        addModifierAndScheduleRemoval(attributeInstance, modifier, duration[data])

        return SkillResult.SUCCESS
    }

    override fun castAtEntity(data: SkillMetadata, target: AbstractEntity): SkillResult {
        val targetEntity = target.bukkitEntity as? LivingEntity ?: return SkillResult.INVALID_TARGET
        val attributeMap = attributeMapAccess.get(targetEntity)
        val modifier = AttributeModifier(Key.key(name[data]), amount[data], operation)
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