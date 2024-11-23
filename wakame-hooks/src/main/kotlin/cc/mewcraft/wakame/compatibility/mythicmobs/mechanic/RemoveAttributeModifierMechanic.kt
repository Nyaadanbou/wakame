package cc.mewcraft.wakame.compatibility.mythicmobs.mechanic

import cc.mewcraft.wakame.attribute.Attribute
import cc.mewcraft.wakame.attribute.AttributeMapAccess
import cc.mewcraft.wakame.attribute.AttributeProvider
import io.lumine.mythic.api.adapters.AbstractEntity
import io.lumine.mythic.api.config.MythicLineConfig
import io.lumine.mythic.api.skills.INoTargetSkill
import io.lumine.mythic.api.skills.ITargetedEntitySkill
import io.lumine.mythic.api.skills.SkillMetadata
import io.lumine.mythic.api.skills.SkillResult
import io.lumine.mythic.api.skills.ThreadSafetyLevel
import io.lumine.mythic.api.skills.placeholders.PlaceholderString
import io.lumine.mythic.core.skills.SkillExecutor
import io.lumine.mythic.core.skills.SkillMechanic
import net.kyori.adventure.key.Key
import org.bukkit.entity.LivingEntity
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File

class RemoveAttributeModifierMechanic(
    manager: SkillExecutor,
    file: File,
    line: String,
    mlc: MythicLineConfig,
) : SkillMechanic(manager, file, line, mlc), ITargetedEntitySkill, INoTargetSkill, KoinComponent {

    init {
        threadSafetyLevel = ThreadSafetyLevel.SYNC_ONLY
    }

    private val attributeProvider: AttributeProvider by inject()
    private val attributeMapAccess: AttributeMapAccess by inject()

    private val attribute: Attribute = mlc.getString(arrayOf("attribute", "attr"))
        ?.let { parsed -> attributeProvider.getSingleton(parsed) }
        ?: throw IllegalArgumentException("Invalid attribute from line: $line")
    private val name: PlaceholderString = mlc.getPlaceholderString(arrayOf("name"), null, *emptyArray())
        ?: throw IllegalArgumentException("Invalid attribute modifier name from line: $line")

    override fun cast(data: SkillMetadata): SkillResult {
        val targetEntity = data.caster.entity.bukkitEntity as? LivingEntity ?: return SkillResult.INVALID_TARGET
        val attributeMap = attributeMapAccess.get(targetEntity).getOrNull() ?: return SkillResult.ERROR
        val attributeInstance = attributeMap.getInstance(attribute) ?: return SkillResult.INVALID_TARGET
        attributeInstance.removeModifier(Key.key(name[data]))

        return SkillResult.SUCCESS
    }

    override fun castAtEntity(data: SkillMetadata, target: AbstractEntity): SkillResult {
        val targetEntity = target.bukkitEntity as? LivingEntity ?: return SkillResult.INVALID_TARGET
        val attributeMap = attributeMapAccess.get(targetEntity).getOrNull() ?: return SkillResult.ERROR
        val attributeInstance = attributeMap.getInstance(attribute) ?: return SkillResult.INVALID_TARGET
        attributeInstance.removeModifier(Key.key(name[data]))

        return SkillResult.SUCCESS
    }
}