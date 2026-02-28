package cc.mewcraft.wakame.hook.impl.mythicmobs.mechanic

import cc.mewcraft.wakame.entity.attribute.*
import cc.mewcraft.wakame.util.runTaskLater
import io.lumine.mythic.api.adapters.AbstractEntity
import io.lumine.mythic.api.config.MythicLineConfig
import io.lumine.mythic.api.skills.*
import io.lumine.mythic.api.skills.placeholders.PlaceholderDouble
import io.lumine.mythic.api.skills.placeholders.PlaceholderInt
import io.lumine.mythic.api.skills.placeholders.PlaceholderString
import io.lumine.mythic.bukkit.utils.collections.expiringmap.ExpirationPolicy
import io.lumine.mythic.bukkit.utils.collections.expiringmap.ExpiringMap
import io.lumine.mythic.core.skills.SkillExecutor
import io.lumine.mythic.core.skills.SkillMechanic
import net.kyori.adventure.key.Key
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitTask
import java.io.File
import java.util.concurrent.TimeUnit

class AttributeModifierMechanic(
    manager: SkillExecutor,
    file: File,
    line: String,
    mlc: MythicLineConfig,
) : SkillMechanic(manager, file, line, mlc), ITargetedEntitySkill, INoTargetSkill {

    companion object {
        private val modifierRemovalTasks = ExpiringMap.builder().variableExpiration().build<Key, BukkitTask>()
    }

    init {
        threadSafetyLevel = ThreadSafetyLevel.SYNC_ONLY
    }

    private val attribute: Attribute = mlc.getString(arrayOf("attribute", "attr"))
        ?.let { parsed -> AttributeProvider.INSTANCE.get(parsed) }
        ?: throw IllegalArgumentException("Invalid attribute from line: $line")
    private val name: PlaceholderString = mlc.getPlaceholderString(arrayOf("name"), null, *emptyArray())
        ?: throw IllegalArgumentException("Invalid attribute modifier name from line: $line")
    private val amount: PlaceholderDouble = mlc.getPlaceholderDouble(arrayOf("amount", "amt", "a"), .0, *emptyArray())
    private val operation: AttributeModifier.Operation = mlc.getEnum(arrayOf("operation", "op"), AttributeModifier.Operation::class.java, AttributeModifier.Operation.ADD)
    private val duration: PlaceholderInt = mlc.getPlaceholderInteger(arrayOf("duration", "dur"), 0, *emptyArray())
    private val replace: Boolean = mlc.getBoolean(arrayOf("replace", "r"), true)

    override fun cast(data: SkillMetadata): SkillResult {
        val targetEntity = data.caster.entity.bukkitEntity as? LivingEntity ?: return SkillResult.INVALID_TARGET
        val attributeMap = AttributeMapAccess.INSTANCE.get(targetEntity).getOrNull() ?: return SkillResult.ERROR
        val modifier = AttributeModifier(Key.key(name[data]), amount[data], operation)
        val attributeInstance = attributeMap.getInstance(attribute) ?: return SkillResult.INVALID_TARGET
        addModifierAndScheduleRemoval(attributeInstance, modifier, duration[data], targetEntity is Player)

        return SkillResult.SUCCESS
    }

    override fun castAtEntity(data: SkillMetadata, target: AbstractEntity): SkillResult {
        val targetEntity = target.bukkitEntity as? LivingEntity ?: return SkillResult.INVALID_TARGET
        val attributeMap = AttributeMapAccess.INSTANCE.get(targetEntity).getOrNull() ?: return SkillResult.ERROR
        val modifier = AttributeModifier(Key.key(name[data]), amount[data], operation)
        val attributeInstance = attributeMap.getInstance(attribute) ?: return SkillResult.INVALID_TARGET
        addModifierAndScheduleRemoval(attributeInstance, modifier, duration[data], targetEntity is Player)

        return SkillResult.SUCCESS
    }

    private fun addModifierAndScheduleRemoval(instance: AttributeInstance, modifier: AttributeModifier, duration: Int, isPlayer: Boolean) {
        if (duration > 0) {
            // 永远需要先取消之前的移除任务, 否则会导致之前的移除任务错误地移除新的 modifier
            modifierRemovalTasks.remove(modifier.id)?.cancel()
            modifierRemovalTasks.put(
                modifier.id,
                runTaskLater(duration.toLong()) { ->
                    instance.removeModifier(modifier)
                },
                ExpirationPolicy.CREATED,
                duration * 50L,
                TimeUnit.MILLISECONDS
            )
        }
        if (replace) {
            instance.removeModifier(modifier)
        }
        if (isPlayer) {
            // 对于玩家, 添加临时属性修饰符, 以避免属性意外永久驻留在玩家存档里
            instance.addTransientModifier(modifier)
        } else {
            instance.addModifier(modifier)
        }
    }
}