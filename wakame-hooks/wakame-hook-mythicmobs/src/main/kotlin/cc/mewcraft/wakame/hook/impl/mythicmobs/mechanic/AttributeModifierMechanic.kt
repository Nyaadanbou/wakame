package cc.mewcraft.wakame.hook.impl.mythicmobs.mechanic

import cc.mewcraft.wakame.attribute.*
import io.lumine.mythic.api.adapters.AbstractEntity
import io.lumine.mythic.api.config.MythicLineConfig
import io.lumine.mythic.api.skills.*
import io.lumine.mythic.api.skills.placeholders.PlaceholderDouble
import io.lumine.mythic.api.skills.placeholders.PlaceholderInt
import io.lumine.mythic.api.skills.placeholders.PlaceholderString
import io.lumine.mythic.bukkit.utils.Schedulers
import io.lumine.mythic.core.skills.SkillExecutor
import io.lumine.mythic.core.skills.SkillMechanic
import net.kyori.adventure.key.Key
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import java.io.File

class AttributeModifierMechanic(
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
    private val amount: PlaceholderDouble = mlc.getPlaceholderDouble(arrayOf("amount", "amt", "a"), .0, *emptyArray())
    private val operation: AttributeModifier.Operation = mlc.getEnum(arrayOf("operation", "op"), AttributeModifier.Operation::class.java, AttributeModifier.Operation.ADD)
    private val duration: PlaceholderInt = mlc.getPlaceholderInteger(arrayOf("duration", "dur"), 0, *emptyArray())
    private val replace: Boolean = mlc.getBoolean(arrayOf("replace", "r"), true)

    override fun cast(data: SkillMetadata): SkillResult {
        val targetEntity = data.caster.entity.bukkitEntity as? LivingEntity ?: return SkillResult.INVALID_TARGET
        val attributeMap = AttributeMapAccess.instance().get(targetEntity).getOrNull() ?: return SkillResult.ERROR
        val modifier = AttributeModifier(Key.key(name[data]), amount[data], operation)
        val attributeInstance = attributeMap.getInstance(attribute) ?: return SkillResult.INVALID_TARGET
        addModifierAndScheduleRemoval(attributeInstance, modifier, duration[data], targetEntity is Player)

        return SkillResult.SUCCESS
    }

    override fun castAtEntity(data: SkillMetadata, target: AbstractEntity): SkillResult {
        val targetEntity = target.bukkitEntity as? LivingEntity ?: return SkillResult.INVALID_TARGET
        val attributeMap = AttributeMapAccess.instance().get(targetEntity).getOrNull() ?: return SkillResult.ERROR
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