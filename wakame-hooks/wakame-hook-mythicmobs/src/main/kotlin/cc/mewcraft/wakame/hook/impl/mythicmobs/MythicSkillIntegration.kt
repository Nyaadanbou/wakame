package cc.mewcraft.wakame.hook.impl.mythicmobs

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.integration.skill.SkillIntegration
import cc.mewcraft.wakame.integration.skill.SkillWrapper
import cc.mewcraft.wakame.integration.skill.SkillWrapperType
import cc.mewcraft.wakame.integration.skill.SkillWrapperTypes
import io.lumine.mythic.api.adapters.AbstractEntity
import io.lumine.mythic.bukkit.BukkitAdapter
import io.lumine.mythic.bukkit.MythicBukkit
import io.lumine.mythic.core.skills.SkillMetadataImpl
import io.lumine.mythic.core.skills.SkillTriggers
import io.lumine.mythic.core.utils.MythicUtil
import org.bukkit.entity.Player

object MythicSkillIntegration : SkillIntegration {

    override fun lookupBlockSkill(id: String): SkillWrapper.Block {
        return MythicBlockSkillWrapper(id)
    }

    override fun lookupInlineSkill(line: String): SkillWrapper.Inline {
        return MythicInlineSkillWrapper(line)
    }
}

class MythicBlockSkillWrapper(
    override val id: String,
) : SkillWrapper.Block {

    private val mythicApi: MythicBukkit
        get() = MythicBukkit.inst()

    override val type: SkillWrapperType
        get() = SkillWrapperTypes.BLOCK

    override fun cast(player: Player) {
        val origin = player.location
        val entityTargets = listOf(MythicUtil.getTargetedEntity(player))
        val locationTargets = listOf(player.getLineOfSight(null, 16).last().location)
        mythicApi.apiHelper.castSkill(player, id, player, origin, entityTargets, locationTargets, 1f)
        LOGGER.info("Trying to running block skill: $id")
    }
}

class MythicInlineSkillWrapper(
    override val line: String,
) : SkillWrapper.Inline {

    private val mythicApi: MythicBukkit
        get() = MythicBukkit.inst()

    override val type: SkillWrapperType
        get() = SkillWrapperTypes.INLINE

    override fun cast(player: Player) {
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
}