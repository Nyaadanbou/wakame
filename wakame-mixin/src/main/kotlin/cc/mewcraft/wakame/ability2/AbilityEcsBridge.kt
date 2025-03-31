package cc.mewcraft.wakame.ability2

import cc.mewcraft.wakame.ability2.component.*
import cc.mewcraft.wakame.ability2.meta.AbilityMeta
import cc.mewcraft.wakame.ability2.trigger.AbilityTriggerVariant
import cc.mewcraft.wakame.ecs.Fleks
import cc.mewcraft.wakame.ecs.bridge.BukkitPlayer
import cc.mewcraft.wakame.ecs.bridge.KoishEntity
import cc.mewcraft.wakame.ecs.bridge.koishify
import cc.mewcraft.wakame.ecs.component.TickCountComponent
import cc.mewcraft.wakame.item.ItemSlot
import cc.mewcraft.wakame.item2.config.property.impl.AbilityOnItem
import team.unnamed.mocha.MochaEngine

object AbilityEcsBridge {

    fun createEcsEntities(
        ability: AbilityOnItem,
        caster: KoishEntity,
        target: KoishEntity,
        phase: StatePhase,
        slot: ItemSlot?,
    ) {
        val metaType = ability.meta.type
        val entity = Fleks.INSTANCE.createEntity { entity ->
            entity += AbilityComponent(
                metaType = metaType,
                phase = phase,
                trigger = ability.trigger,
                variant = ability.variant,
                mochaEngine = MochaEngine.createStandard()
            )
            entity += listOf(ability.meta.params)
            entity += CastBy(caster.unwrap())
            entity += TargetTo(target.unwrap())
            ability.manaCost?.let { entity += ManaCost(it) }
            slot?.let { entity += AtSlot(it) }
            entity += TickCountComponent(0)
        }

        if (caster.has(AbilityContainer)) {
            val container = caster[AbilityContainer]
            container[metaType] = entity
        }
    }

    fun createEcsEntities(
        abilityMeta: AbilityMeta,
        caster: KoishEntity,
        target: KoishEntity,
        phase: StatePhase,
    ) {
        val metaType = abilityMeta.type
        val entity = Fleks.INSTANCE.createEntity { entity ->
            entity += AbilityComponent(
                metaType = metaType,
                phase = phase,
                trigger = null,
                variant = AbilityTriggerVariant.any(),
                mochaEngine = MochaEngine.createStandard()
            )
            entity += listOf(abilityMeta.params)
            entity += CastBy(caster.unwrap())
            entity += TargetTo(target.unwrap())
        }

        if (caster.has(AbilityContainer)) {
            val container = caster[AbilityContainer]
            container[metaType] = entity
        }
    }

    fun getPlayerAllSingleAbilities(bukkitPlayer: BukkitPlayer): List<AbilityInfo> {
        return bukkitPlayer.koishify()[AbilityContainer].convertToSingleAbilityList()
    }
}