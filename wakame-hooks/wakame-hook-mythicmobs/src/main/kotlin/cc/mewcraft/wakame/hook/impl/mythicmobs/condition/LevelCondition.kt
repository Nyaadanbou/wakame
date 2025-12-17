package cc.mewcraft.wakame.hook.impl.mythicmobs.condition

import cc.mewcraft.wakame.integration.playerlevel.PlayerLevelIntegration
import io.lumine.mythic.api.adapters.AbstractEntity
import io.lumine.mythic.api.adapters.AbstractLocation
import io.lumine.mythic.api.config.MythicLineConfig
import io.lumine.mythic.api.skills.conditions.IEntityCondition
import io.lumine.mythic.api.skills.conditions.ILocationCondition
import io.lumine.mythic.bukkit.BukkitAdapter
import io.lumine.mythic.bukkit.utils.numbers.RangedInt
import io.lumine.mythic.core.skills.SkillCondition
import org.bukkit.entity.Player

/**
 * 判断玩家的冒险等级.
 */
class LevelCondition(
    line: String,
    mlc: MythicLineConfig,
) : SkillCondition(line), IEntityCondition, ILocationCondition {

    private val level: RangedInt = RangedInt(mlc.getString(arrayOf("level", "amount", "a"), ">0", *arrayOfNulls<String>(0)))
    private val searchRadius: Double = mlc.getDouble(arrayOf("radius", "r"), .0)

    override fun check(target: AbstractEntity): Boolean {
        val bukkitEntity = target.bukkitEntity
        if (bukkitEntity is Player) {
            // 用于召唤 Boss 时检测玩家的冒险等级
            return checkLevel(bukkitEntity)
        } else {
            // 用于在随机生成条件为 ADD 的时候判断
            val location = bukkitEntity.location
            return location.getNearbyPlayers(searchRadius).firstOrNull()?.let { checkLevel(it) } == true
        }
    }

    override fun check(target: AbstractLocation): Boolean {
        // 用于在随机生成条件为 ADD 的时候判断
        val location = BukkitAdapter.adapt(target)
        return location.getNearbyPlayers(searchRadius).firstOrNull()?.let { checkLevel(it) } == true
    }

    private fun checkLevel(player: Player): Boolean {
        val level = PlayerLevelIntegration.get(player.uniqueId)
            ?: return false
        return this.level.equals(level)
    }
}