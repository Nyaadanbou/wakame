package cc.mewcraft.wakame.compatibility.mythicmobs.condition

import cc.mewcraft.adventurelevel.level.category.LevelCategory
import cc.mewcraft.adventurelevel.plugin.AdventureLevelProvider
import io.lumine.mythic.api.adapters.AbstractEntity
import io.lumine.mythic.api.adapters.AbstractLocation
import io.lumine.mythic.api.config.MythicLineConfig
import io.lumine.mythic.api.skills.conditions.IEntityCondition
import io.lumine.mythic.api.skills.conditions.ILocationCondition
import io.lumine.mythic.bukkit.BukkitAdapter
import io.lumine.mythic.bukkit.utils.numbers.RangedInt
import io.lumine.mythic.core.skills.SkillCondition
import org.bukkit.entity.Entity
import org.bukkit.entity.Player


/**
 * 判断玩家的冒险等级.
 */
class LevelCondition(
    line: String,
    mlc: MythicLineConfig,
) : SkillCondition(line), IEntityCondition, ILocationCondition {
    private val level: RangedInt = RangedInt(mlc.getString(arrayOf("level"), ">0", *arrayOfNulls<String>(0)))
    private val searchRadius: Double = mlc.getDouble(arrayOf("radius", "r"), .0)

    override fun check(target: AbstractEntity): Boolean {
        return checkLevel(target.bukkitEntity)
    }

    override fun check(target: AbstractLocation): Boolean {
        val location = BukkitAdapter.adapt(target)

        return location.getNearbyPlayers(searchRadius)
            .firstOrNull()
            ?.let { checkLevel(it) }
            ?: false
    }

    private fun checkLevel(bukkitEntity: Entity): Boolean {
        if (bukkitEntity !is Player) {
            return false
        } else {
            val playerData = AdventureLevelProvider.get().playerDataManager().load(bukkitEntity)
            val playerLevel = playerData.getLevel(LevelCategory.PRIMARY) // 数据未加载完毕时, 会返回 0
            val levelNumber = playerLevel.level
            return level.equals(levelNumber)
        }
    }
}