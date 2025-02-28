package cc.mewcraft.wakame.hook.impl.mythicmobs.mechanic

import cc.mewcraft.wakame.attribute.Attribute
import cc.mewcraft.wakame.attribute.AttributeMapAccess
import cc.mewcraft.wakame.attribute.AttributeProvider
import io.lumine.mythic.api.adapters.AbstractEntity
import io.lumine.mythic.api.config.MythicLineConfig
import io.lumine.mythic.api.skills.*
import io.lumine.mythic.api.skills.placeholders.PlaceholderString
import io.lumine.mythic.core.skills.SkillExecutor
import io.lumine.mythic.core.skills.SkillMechanic
import net.kyori.adventure.key.Key
import org.bukkit.entity.LivingEntity
import java.io.File

class RemoveAttributeModifierMechanic(
    manager: SkillExecutor,
    file: File,
    line: String,
    mlc: MythicLineConfig,
) : SkillMechanic(manager, file, line, mlc), ITargetedEntitySkill, INoTargetSkill {
    init {
        threadSafetyLevel = ThreadSafetyLevel.SYNC_ONLY
    }

    private val attribute: Attribute = mlc.getString(arrayOf("attribute", "attr"))
        ?.let { parsed -> AttributeProvider.instance().get(parsed) }
        ?: throw IllegalArgumentException("Invalid attribute from line: $line")
    private val name: PlaceholderString = mlc.getPlaceholderString(arrayOf("name"), null, *emptyArray())
        ?: throw IllegalArgumentException("Invalid attribute modifier name from line: $line")

    override fun cast(data: SkillMetadata): SkillResult {
        val targetEntity = data.caster.entity.bukkitEntity as? LivingEntity ?: return SkillResult.INVALID_TARGET
        val attributeMap = AttributeMapAccess.instance().get(targetEntity).getOrNull() ?: return SkillResult.ERROR
        val attributeInstance = attributeMap.getInstance(attribute) ?: return SkillResult.INVALID_TARGET
        attributeInstance.removeModifier(Key.key(name[data]))

        return SkillResult.SUCCESS
    }

    override fun castAtEntity(data: SkillMetadata, target: AbstractEntity): SkillResult {
        val targetEntity = target.bukkitEntity as? LivingEntity ?: return SkillResult.INVALID_TARGET
        val attributeMap = AttributeMapAccess.instance().get(targetEntity).getOrNull() ?: return SkillResult.ERROR
        val attributeInstance = attributeMap.getInstance(attribute) ?: return SkillResult.INVALID_TARGET
        attributeInstance.removeModifier(Key.key(name[data]))

        return SkillResult.SUCCESS
    }
}