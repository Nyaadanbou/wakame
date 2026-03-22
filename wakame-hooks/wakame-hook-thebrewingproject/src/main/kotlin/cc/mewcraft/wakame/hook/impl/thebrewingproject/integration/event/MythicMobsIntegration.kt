package cc.mewcraft.wakame.hook.impl.thebrewingproject.integration.event

import cc.mewcraft.wakame.integration.skill.SkillIntegration
import dev.jsinco.brewery.api.event.EventData
import dev.jsinco.brewery.api.event.EventProbability
import dev.jsinco.brewery.api.event.IntegrationEvent
import dev.jsinco.brewery.api.meta.MetaDataType
import dev.jsinco.brewery.api.util.BreweryKey
import dev.jsinco.brewery.api.util.Holder
import dev.jsinco.brewery.bukkit.api.BukkitAdapter
import dev.jsinco.brewery.bukkit.api.integration.EventIntegration
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.*


/**
 * TBP 自定义事件集成: 释放 MythicMobs Mechanic.
 *
 * 在 TBP 配置文件中使用:
 * - `mythicmobs:skill{mythicmobs:block=<SkillName>}` — 按 ID 施放 MythicMobs Mechanic
 * - `mythicmobs:skill{mythicmobs:inline=<MechanicLine>}` — 施放内联 MythicMobs Mechanic
 */
class MythicMobsIntegration : EventIntegration<MythicMobsIntegration.MythicMobsEvent> {

    companion object {
        private const val MYTHICMOBS = "mythicmobs"

        // Event key (唯一)
        private val SKILL_KEY: BreweryKey = BreweryKey.parse("skill", MYTHICMOBS)

        // Metadata keys (互斥, 用于区分施放类型)
        private val BLOCK_META_KEY: Key = Key.key(MYTHICMOBS, "block")
        private val INLINE_META_KEY: Key = Key.key(MYTHICMOBS, "inline")
    }

    override fun eClass(): Class<MythicMobsEvent> = MythicMobsEvent::class.java

    override fun listEventKeys(): List<BreweryKey> = listOf(SKILL_KEY)

    override fun convertToEvent(event: EventData): Optional<MythicMobsEvent> {
        if (event.key() != SKILL_KEY) return Optional.empty()

        // 优先检查 block, 再检查 inline
        val blockSkillId = event.data(BLOCK_META_KEY, MetaDataType.STRING)
        if (blockSkillId != null) {
            return Optional.of(BlockSkillEvent(blockSkillId))
        }

        val inlineMechanic = event.data(INLINE_META_KEY, MetaDataType.STRING)
        if (inlineMechanic != null) {
            return Optional.of(InlineSkillEvent(inlineMechanic))
        }

        return Optional.empty()
    }

    override fun convertToData(event: MythicMobsEvent): EventData {
        return when (event) {
            is BlockSkillEvent -> EventData(SKILL_KEY)
                .withData(BLOCK_META_KEY, MetaDataType.STRING, event.skillId)

            is InlineSkillEvent -> EventData(SKILL_KEY)
                .withData(INLINE_META_KEY, MetaDataType.STRING, event.mechanic)
        }
    }

    override fun getId(): String = MYTHICMOBS

    override fun isEnabled(): Boolean = Bukkit.getPluginManager().getPlugin("MythicMobs") != null

    /**
     * MythicMobs 事件的密封接口.
     */
    sealed interface MythicMobsEvent : IntegrationEvent {
        override fun run(player: Holder.Player) {
            BukkitAdapter.toPlayer(player).ifPresent(this::run)
        }

        fun run(player: Player)
    }

    /**
     * 按技能 ID 施放 MythicMobs Mechanic.
     *
     * 配置格式: `mythicmobs:skill{mythicmobs:block=<SkillName>}`
     */
    data class BlockSkillEvent(val skillId: String) : MythicMobsEvent {
        override fun run(player: Player) {
            SkillIntegration.castBlockSkill(player, skillId)
        }

        override fun key(): BreweryKey = SKILL_KEY
        override fun displayName(): Component = Component.text("MythicMobs Block Skill: $skillId")
        override fun probability(): EventProbability = EventProbability.NONE
    }

    /**
     * 施放内联 MythicMobs Mechanic.
     *
     * 配置格式: `mythicmobs:skill{mythicmobs:inline=<MechanicLine>}`
     */
    data class InlineSkillEvent(val mechanic: String) : MythicMobsEvent {
        override fun run(player: Player) {
            SkillIntegration.castInlineSkill(player, mechanic)
        }

        override fun key(): BreweryKey = SKILL_KEY
        override fun displayName(): Component = Component.text("MythicMobs Inline Skill: $mechanic")
        override fun probability(): EventProbability = EventProbability.NONE
    }
}