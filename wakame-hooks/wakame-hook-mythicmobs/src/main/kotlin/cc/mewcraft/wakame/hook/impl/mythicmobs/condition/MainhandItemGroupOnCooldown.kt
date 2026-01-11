package cc.mewcraft.wakame.hook.impl.mythicmobs.condition

import cc.mewcraft.wakame.item.extension.isOnCooldown
import io.lumine.mythic.api.adapters.AbstractEntity
import io.lumine.mythic.api.config.MythicLineConfig
import io.lumine.mythic.api.skills.ThreadSafetyLevel
import io.lumine.mythic.api.skills.conditions.IEntityCondition
import io.lumine.mythic.core.skills.SkillCondition
import org.bukkit.entity.Player

class MainhandItemGroupOnCooldown(
    line: String,
    mlc: MythicLineConfig,
) : SkillCondition(line), IEntityCondition {

    init {
        threadSafetyLevel = ThreadSafetyLevel.SYNC_ONLY
    }

    override fun check(target: AbstractEntity): Boolean {
        val bukkitEntity = target.bukkitEntity
        if (bukkitEntity !is Player) {
            return false
        }

        val equipment = bukkitEntity.equipment ?: return false
        val itemInMainHand = equipment.itemInMainHand

        return itemInMainHand.isOnCooldown(bukkitEntity)
    }
}