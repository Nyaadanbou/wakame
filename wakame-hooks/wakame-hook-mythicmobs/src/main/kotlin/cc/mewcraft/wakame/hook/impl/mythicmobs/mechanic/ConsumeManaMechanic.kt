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

class ConsumeManaMechanic(
    manager: SkillExecutor,
    file: File,
    line: String,
    mlc: MythicLineConfig,
) : SkillMechanic(manager, file, line, mlc), ITargetedEntitySkill, INoTargetSkill {

    init {
        threadSafetyLevel = ThreadSafetyLevel.SYNC_ONLY
    }

    private val amount: PlaceholderDouble = mlc.getPlaceholderDouble(arrayOf("amount", "a"), .0, *arrayOfNulls(0))

    override fun cast(data: SkillMetadata): SkillResult {
        val player = data.caster?.entity?.bukkitEntity as? Player ?: return SkillResult.INVALID_TARGET
        val amount = amount[data]
        return consume(player, amount)
    }

    override fun castAtEntity(data: SkillMetadata, target: AbstractEntity): SkillResult {
        val player = target.bukkitEntity as? Player ?: return SkillResult.INVALID_TARGET
        val amount = amount[data]
        return consume(player, amount)
    }

    private fun consume(player: Player, amount: Double): SkillResult {
        val result = PlayerManaIntegration.consumeMana(player, amount)
        return if (result) {
            SkillResult.SUCCESS
        } else {
            SkillResult.CONDITION_FAILED
        }
    }
}