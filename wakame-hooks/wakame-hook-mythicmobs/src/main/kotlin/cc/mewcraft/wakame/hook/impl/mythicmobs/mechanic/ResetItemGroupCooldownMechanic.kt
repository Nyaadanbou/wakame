package cc.mewcraft.wakame.hook.impl.mythicmobs.mechanic

import cc.mewcraft.wakame.item.extension.removeCooldown
import cc.mewcraft.wakame.util.item.takeUnlessEmpty
import io.lumine.mythic.api.adapters.AbstractEntity
import io.lumine.mythic.api.config.MythicLineConfig
import io.lumine.mythic.api.skills.*
import io.lumine.mythic.core.skills.SkillExecutor
import io.lumine.mythic.core.skills.SkillMechanic
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import java.io.File

class ResetItemGroupCooldownMechanic(
    manager: SkillExecutor, file: File, line: String, mlc: MythicLineConfig,
) : SkillMechanic(manager, file, line, mlc), ITargetedEntitySkill, INoTargetSkill {

    init {
        threadSafetyLevel = ThreadSafetyLevel.SYNC_ONLY
    }

    private val equipmentSlot: EquipmentSlot = mlc.getEnum(arrayOf("slot", "s"), EquipmentSlot::class.java, EquipmentSlot.HAND)

    override fun cast(data: SkillMetadata): SkillResult {
        val casterPlayer = data.caster.entity.bukkitEntity as? Player ?: return SkillResult.INVALID_TARGET
        val itemInEquipmentSlot = casterPlayer.inventory.getItem(equipmentSlot).takeUnlessEmpty() ?: return SkillResult.ERROR
        itemInEquipmentSlot.removeCooldown(casterPlayer)
        return SkillResult.SUCCESS
    }

    override fun castAtEntity(data: SkillMetadata, target: AbstractEntity): SkillResult {
        val targetPlayer = target.bukkitEntity as? Player ?: return SkillResult.INVALID_TARGET
        val itemInEquipmentSlot = targetPlayer.inventory.getItem(equipmentSlot).takeUnlessEmpty() ?: return SkillResult.ERROR
        itemInEquipmentSlot.removeCooldown(targetPlayer)
        return SkillResult.SUCCESS
    }
}