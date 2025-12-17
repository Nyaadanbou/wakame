package cc.mewcraft.wakame.hook.impl.mythicmobs.condition

import cc.mewcraft.wakame.integration.playermana.PlayerManaIntegration
import io.lumine.mythic.api.adapters.AbstractEntity
import io.lumine.mythic.api.config.MythicLineConfig
import io.lumine.mythic.api.skills.ThreadSafetyLevel
import io.lumine.mythic.api.skills.conditions.IEntityCondition
import io.lumine.mythic.bukkit.utils.numbers.RangedDouble
import io.lumine.mythic.core.skills.SkillCondition
import org.bukkit.entity.Player

class ManaCondition(
    line: String,
    mlc: MythicLineConfig,
) : SkillCondition(line), IEntityCondition {

    init {
        threadSafetyLevel = ThreadSafetyLevel.SYNC_ONLY
    }

    private val level: RangedDouble = RangedDouble(mlc.getString(arrayOf("mana", "amount", "a"), ">0", *arrayOfNulls<String>(0)))

    override fun check(target: AbstractEntity): Boolean {
        val player = target.bukkitEntity as? Player ?: return false
        val mana = PlayerManaIntegration.getMana(player)
        return level.equals(mana)
    }
}