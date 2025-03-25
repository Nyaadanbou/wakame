package cc.mewcraft.wakame.ability2

import cc.mewcraft.wakame.ability2.component.AbilityComponent
import cc.mewcraft.wakame.ability2.component.AbilityContainer
import cc.mewcraft.wakame.ability2.component.AtSlot
import cc.mewcraft.wakame.ability2.component.CastBy
import cc.mewcraft.wakame.ability2.component.ManaCost
import cc.mewcraft.wakame.ability2.component.TargetTo
import cc.mewcraft.wakame.ability2.data.StatePhase
import cc.mewcraft.wakame.ability2.meta.AbilityMeta
import cc.mewcraft.wakame.ability2.trigger.AbilityTriggerVariant
import cc.mewcraft.wakame.ecs.Fleks
import cc.mewcraft.wakame.ecs.bridge.BukkitPlayer
import cc.mewcraft.wakame.ecs.bridge.KoishEntity
import cc.mewcraft.wakame.ecs.bridge.koishify
import cc.mewcraft.wakame.ecs.component.TickCountComponent
import cc.mewcraft.wakame.item.ItemSlot
import team.unnamed.mocha.MochaEngine

object AbilityEcsBridge {

    fun createEcsEntities(
        ability: AbilityObject,
        caster: KoishEntity,
        target: KoishEntity,
        phase: StatePhase,
        slot: ItemSlot?,
    ) {
        for (metaType in ability.meta.dataConfig) {
            val entity = Fleks.createEntity { entity ->
                entity += AbilityComponent(
                    createdBy = ability.meta,
                    metaType = metaType,
                    phase = phase,
                    trigger = ability.trigger,
                    variant = ability.variant,
                    mochaEngine = MochaEngine.createStandard()
                )
                ability.meta.dataConfig[metaType]?.let { entity += listOf(it) }
                entity += CastBy(caster.entity)
                entity += TargetTo(target.entity)
                ability.manaCost?.let { entity += ManaCost(it) }
                slot?.let { entity += AtSlot(it) }
                entity += TickCountComponent(0)
            }

            if (caster.has(AbilityContainer)) {
                val container = caster[AbilityContainer]
                container[metaType] = entity
            }
        }
    }

    fun createEcsEntities(
        ability: AbilityMeta,
        caster: KoishEntity,
        target: KoishEntity,
        phase: StatePhase,
    ) {
        for (metaType in ability.dataConfig) {
            val entity = Fleks.createEntity { entity ->
                entity += AbilityComponent(
                    createdBy = ability,
                    metaType = metaType,
                    phase = phase,
                    trigger = null,
                    variant = AbilityTriggerVariant.any(),
                    mochaEngine = MochaEngine.createStandard()
                )
                ability.dataConfig[metaType]?.let { entity += listOf(it) }
                entity += CastBy(caster.entity)
                entity += TargetTo(target.entity)
            }

            if (caster.has(AbilityContainer)) {
                val container = caster[AbilityContainer]
                container[metaType] = entity
            }
        }
    }

    fun getPlayerAllSingleAbilities(bukkitPlayer: BukkitPlayer): List<SingleAbility> {
        return bukkitPlayer.koishify()[AbilityContainer].convertToSingleAbilityList()
    }
}