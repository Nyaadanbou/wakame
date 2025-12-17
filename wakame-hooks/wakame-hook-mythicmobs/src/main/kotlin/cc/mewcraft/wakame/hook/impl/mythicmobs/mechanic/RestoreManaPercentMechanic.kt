package cc.mewcraft.wakame.hook.impl.mythicmobs.mechanic

import cc.mewcraft.wakame.integration.playermana.PlayerManaIntegration
import io.lumine.mythic.api.adapters.AbstractEntity
import io.lumine.mythic.api.config.MythicLineConfig
import io.lumine.mythic.api.skills.*
import io.lumine.mythic.api.skills.placeholders.PlaceholderDouble
import io.lumine.mythic.core.skills.SkillExecutor
import io.lumine.mythic.core.skills.SkillMechanic
import org.bukkit.entity.Player
import java.io.File
import kotlin.math.min

class RestoreManaPercentMechanic(
    manager: SkillExecutor,
    file: File,
    line: String,
    mlc: MythicLineConfig,
) : SkillMechanic(manager, file, line, mlc), ITargetedEntitySkill, INoTargetSkill {

    init {
        threadSafetyLevel = ThreadSafetyLevel.SYNC_ONLY
    }

    private val multiplier: PlaceholderDouble = mlc.getPlaceholderDouble(arrayOf("multiplier", "m", "amount", "a"), .1, *arrayOfNulls(0))

    override fun cast(data: SkillMetadata): SkillResult {
        val player = data.caster?.entity?.bukkitEntity as? Player ?: return SkillResult.INVALID_TARGET
        val multiplier = multiplier[data]
        restorePercent(player, multiplier)
        return SkillResult.SUCCESS
    }

    override fun castAtEntity(data: SkillMetadata, target: AbstractEntity): SkillResult {
        val player = target.bukkitEntity as? Player ?: return SkillResult.INVALID_TARGET
        val multiplier = multiplier[data]
        restorePercent(player, multiplier)
        return SkillResult.SUCCESS
    }

    private fun restorePercent(player: Player, multiplier: Double) {
        val mana = PlayerManaIntegration.getMana(player)
        val maxMana = PlayerManaIntegration.getMaxMana(player)
        val resultMana = min(maxMana, mana + maxMana * multiplier)
        PlayerManaIntegration.setMana(player, resultMana)
    }
}