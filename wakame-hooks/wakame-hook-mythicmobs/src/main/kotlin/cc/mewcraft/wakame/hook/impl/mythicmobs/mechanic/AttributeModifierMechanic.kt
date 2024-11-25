package cc.mewcraft.wakame.hook.impl.mythicmobs.mechanic

import cc.mewcraft.wakame.attribute.Attribute
import cc.mewcraft.wakame.attribute.AttributeInstance
import cc.mewcraft.wakame.attribute.AttributeMapAccess
import cc.mewcraft.wakame.attribute.AttributeModifier
import cc.mewcraft.wakame.attribute.AttributeProvider
import io.lumine.mythic.api.adapters.AbstractEntity
import io.lumine.mythic.api.config.MythicLineConfig
import io.lumine.mythic.api.skills.INoTargetSkill
import io.lumine.mythic.api.skills.ITargetedEntitySkill
import io.lumine.mythic.api.skills.SkillMetadata
import io.lumine.mythic.api.skills.SkillResult
import io.lumine.mythic.api.skills.ThreadSafetyLevel
import io.lumine.mythic.api.skills.placeholders.PlaceholderDouble
import io.lumine.mythic.api.skills.placeholders.PlaceholderInt
import io.lumine.mythic.api.skills.placeholders.PlaceholderString
import io.lumine.mythic.bukkit.utils.Schedulers
import io.lumine.mythic.core.skills.SkillExecutor
import io.lumine.mythic.core.skills.SkillMechanic
import net.kyori.adventure.key.Key
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File

class AttributeModifierMechanic(
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
    private val amount: PlaceholderDouble = mlc.getPlaceholderDouble(arrayOf("amount", "amt", "a"), .0, *emptyArray())
    private val operation: AttributeModifier.Operation = mlc.getEnum(arrayOf("operation", "op"), AttributeModifier.Operation::class.java, AttributeModifier.Operation.ADD)
    private val duration: PlaceholderInt = mlc.getPlaceholderInteger(arrayOf("duration", "dur"), 0, *emptyArray())
    private val replace: Boolean = mlc.getBoolean(arrayOf("replace", "r"), true)

    override fun cast(data: SkillMetadata): SkillResult {
        val targetEntity = data.caster.entity.bukkitEntity as? LivingEntity ?: return SkillResult.INVALID_TARGET
        val attributeMap = attributeMapAccess.get(targetEntity).getOrNull() ?: return SkillResult.ERROR
        val modifier = AttributeModifier(Key.key(name[data]), amount[data], operation)
        val attributeInstance = attributeMap.getInstance(attribute) ?: return SkillResult.INVALID_TARGET
        addModifierAndScheduleRemoval(attributeInstance, modifier, duration[data], targetEntity is Player)

        return SkillResult.SUCCESS
    }

    override fun castAtEntity(data: SkillMetadata, target: AbstractEntity): SkillResult {
        val targetEntity = target.bukkitEntity as? LivingEntity ?: return SkillResult.INVALID_TARGET
        val attributeMap = attributeMapAccess.get(targetEntity).getOrNull() ?: return SkillResult.ERROR
        val modifier = AttributeModifier(Key.key(name[data]), amount[data], operation)
        val attributeInstance = attributeMap.getInstance(attribute) ?: return SkillResult.INVALID_TARGET
        addModifierAndScheduleRemoval(attributeInstance, modifier, duration[data], targetEntity is Player)

        return SkillResult.SUCCESS
    }

    private fun addModifierAndScheduleRemoval(attributeInstance: AttributeInstance, modifier: AttributeModifier, duration: Int, isPlayer: Boolean) {
        if (isPlayer) {
            // 给玩家添加临时的属性修饰符, 避免属性意外永久驻留在玩家存档里
            if (replace) {
                attributeInstance.removeModifier(modifier.id)
            }
            attributeInstance.addTransientModifier(modifier)
        } else {
            if (replace) {
                attributeInstance.removeModifier(modifier.id)
            }
            attributeInstance.addModifier(modifier)
        }
        if (duration > 0) {
            Schedulers.sync().runLater({
                attributeInstance.removeModifier(modifier)
            }, duration.toLong())
        }
    }
}