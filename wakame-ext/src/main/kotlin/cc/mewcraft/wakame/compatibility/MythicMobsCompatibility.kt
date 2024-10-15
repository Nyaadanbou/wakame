package cc.mewcraft.wakame.compatibility

import cc.mewcraft.wakame.attribute.*
import io.lumine.mythic.api.adapters.AbstractEntity
import io.lumine.mythic.api.config.MythicLineConfig
import io.lumine.mythic.api.skills.ITargetedEntitySkill
import io.lumine.mythic.api.skills.SkillMetadata
import io.lumine.mythic.api.skills.SkillResult
import io.lumine.mythic.api.skills.placeholders.PlaceholderDouble
import io.lumine.mythic.bukkit.events.MythicMechanicLoadEvent
import io.lumine.mythic.core.skills.SkillExecutor
import io.lumine.mythic.core.skills.SkillMechanic
import net.kyori.adventure.key.Key
import org.bukkit.entity.Entity
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File

class MythicMobsCompatibilityListener : Listener {
    @EventHandler
    private fun on(e: MythicMechanicLoadEvent) {
        when (e.mechanicName.lowercase()) {
            "wakameattributeadd" -> e.register(
                MythicMobsWakameAttributeAddMechanic(
                    e.container.manager,
                    e.container.file,
                    e.config.line,
                    e.config
                )
            )

            "wakameattributeremove" -> e.register(
                MythicMobsWakameAttributeRemoveMechanic(
                    e.container.manager,
                    e.container.file,
                    e.config.line,
                    e.config
                )
            )
        }
    }
}

private class MythicMobsWakameAttributeAddMechanic(
    manager: SkillExecutor,
    file: File,
    line: String,
    mlc: MythicLineConfig,
) : SkillMechanic(manager, file, line, mlc), ITargetedEntitySkill {
    private val attribute: Attribute = mlc.getString(arrayOf("attribute", "attr"))?.let { MythicMobsCompatibilitySupport.attributeProvider.getBy(it) }
        ?: MythicMobsCompatibilitySupport.attributeProvider.empty()
    private val id: Key? = mlc.getString(arrayOf("id"))?.let { Key.key(it) }
    private val amount: PlaceholderDouble = mlc.getPlaceholderDouble(arrayOf("amount", "a"), 0.0)
    private val operation: AttributeModifier.Operation = mlc.getString(arrayOf("operation", "op"), "add").let { AttributeModifier.Operation.byKeyOrThrow(it) }

    override fun castAtEntity(metadata: SkillMetadata, target: AbstractEntity): SkillResult {
        val id = id ?: return SkillResult.INVALID_CONFIG
        val entity = target.bukkitEntity ?: return SkillResult.INVALID_TARGET
        val attributeMap = MythicMobsCompatibilitySupport.attributeAccessors.get<Entity>().getAttributeMap(entity)

        val attributeInstance = attributeMap.getInstance(attribute) ?: return SkillResult.INVALID_TARGET
        val modifier = AttributeModifier(
            id = id,
            amount = amount.get(target),
            operation = operation
        )

        attributeInstance.addModifier(modifier)
        return SkillResult.SUCCESS
    }
}

private class MythicMobsWakameAttributeRemoveMechanic(
    manager: SkillExecutor,
    file: File,
    line: String,
    mlc: MythicLineConfig,
) : SkillMechanic(manager, file, line, mlc), ITargetedEntitySkill {
    private val attribute: Attribute = mlc.getString(arrayOf("attribute", "attr"))?.let { MythicMobsCompatibilitySupport.attributeProvider.getBy(it) }
        ?: MythicMobsCompatibilitySupport.attributeProvider.empty()
    private val id: Key? = mlc.getString(arrayOf("id"))?.let { Key.key(it) }

    override fun castAtEntity(metadata: SkillMetadata, target: AbstractEntity): SkillResult {
        val id = id ?: return SkillResult.INVALID_CONFIG
        val entity = target.bukkitEntity ?: return SkillResult.INVALID_TARGET
        val attributeMap = MythicMobsCompatibilitySupport.attributeAccessors.get<Entity>().getAttributeMap(entity)

        val attributeInstance = attributeMap.getInstance(attribute) ?: return SkillResult.INVALID_TARGET
        val modifier = attributeInstance.getModifier(id) ?: return SkillResult.INVALID_TARGET

        attributeInstance.removeModifier(modifier)
        return SkillResult.SUCCESS
    }
}

private object MythicMobsCompatibilitySupport : KoinComponent {
    val attributeProvider: AttributeProvider by inject()
    val attributeAccessors: AttributeAccessors by inject()
}