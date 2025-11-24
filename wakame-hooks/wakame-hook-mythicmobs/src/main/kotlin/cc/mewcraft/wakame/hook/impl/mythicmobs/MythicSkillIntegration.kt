package cc.mewcraft.wakame.hook.impl.mythicmobs

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.integration.skill.SkillIntegration
import io.lumine.mythic.api.adapters.AbstractEntity
import io.lumine.mythic.bukkit.BukkitAdapter
import io.lumine.mythic.bukkit.MythicBukkit
import io.lumine.mythic.core.skills.SkillMetadataImpl
import io.lumine.mythic.core.skills.SkillTriggers
import io.lumine.mythic.core.utils.MythicUtil
import org.bukkit.entity.Player
import kotlin.jvm.optionals.getOrNull

object MythicSkillIntegration : SkillIntegration {

    private val mythicApi: MythicBukkit
        get() = MythicBukkit.inst()

    override fun castBlockSkill(player: Player, id: String) {
        val origin = player.location
        val entityTargets = listOf(MythicUtil.getTargetedEntity(player))
        val locationTargets = listOf(player.getLineOfSight(null, 16).last().location)
        mythicApi.apiHelper.castSkill(player, id, player, origin, entityTargets, locationTargets, 1f)
        LOGGER.info("Trying to running block skill: $id")
    }

    override fun castInlineSkill(player: Player, line: String) {
        val line = "[ - $line ]"
        val entity: AbstractEntity = BukkitAdapter.adapt(player)
        val caster = mythicApi.skillManager.getCaster(entity)
        val skill = mythicApi.skillManager.getSkill(line).orElse(null)
        val entityTargets = listOf(entity)
        val locationTargets = listOf(player.getLineOfSight(null, 16).last().location).map(BukkitAdapter::adapt)
        val meta = SkillMetadataImpl(SkillTriggers.API, caster, entity, entity.location, entityTargets, locationTargets, 1f)
        if (skill == null) {
            LOGGER.error("Invalid line supplied to MythicInlineSkillWrapper: $line")
        } else {
            skill.execute(meta)
            LOGGER.info("Trying to running inline skill: $line")
        }
    }

    override fun isCooldown(player: Player, id: String): Boolean {
        val skill = mythicApi.skillManager.getSkill(id).getOrNull() ?: return false
        val caster = mythicApi.skillManager.getCaster(BukkitAdapter.adapt(player))
        return skill.onCooldown(caster)
    }
}
