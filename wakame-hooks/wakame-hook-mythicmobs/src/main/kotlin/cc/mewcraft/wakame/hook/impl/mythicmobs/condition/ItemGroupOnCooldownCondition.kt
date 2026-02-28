package cc.mewcraft.wakame.hook.impl.mythicmobs.condition

import cc.mewcraft.wakame.item.extension.isOnCooldown
import cc.mewcraft.wakame.util.item.takeUnlessEmpty
import io.lumine.mythic.api.adapters.AbstractEntity
import io.lumine.mythic.api.config.MythicLineConfig
import io.lumine.mythic.api.skills.ThreadSafetyLevel
import io.lumine.mythic.api.skills.conditions.IEntityCondition
import io.lumine.mythic.core.skills.SkillCondition
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot

class ItemGroupOnCooldownCondition(
    line: String, mlc: MythicLineConfig,
) : SkillCondition(line), IEntityCondition {

    init {
        threadSafetyLevel = ThreadSafetyLevel.SYNC_ONLY
    }

    private val equipmentSlot: EquipmentSlot = mlc.getEnum(arrayOf("slot", "s"), EquipmentSlot::class.java, EquipmentSlot.HAND)

    override fun check(target: AbstractEntity): Boolean {
        val bukkitEntity = target.bukkitEntity
        if (bukkitEntity !is Player) return false
        val itemInEquipmentSlot = bukkitEntity.inventory.getItem(equipmentSlot).takeUnlessEmpty() ?: return false
        return itemInEquipmentSlot.isOnCooldown(bukkitEntity)
    }
}