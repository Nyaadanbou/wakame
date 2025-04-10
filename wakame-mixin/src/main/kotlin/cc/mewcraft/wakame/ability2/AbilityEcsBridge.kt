package cc.mewcraft.wakame.ability2

import cc.mewcraft.wakame.ability2.component.AbilityComponent
import cc.mewcraft.wakame.ability2.component.AbilityContainer
import cc.mewcraft.wakame.ability2.component.AtSlot
import cc.mewcraft.wakame.ability2.component.CastBy
import cc.mewcraft.wakame.ability2.component.ManaCost
import cc.mewcraft.wakame.ability2.component.TargetTo
import cc.mewcraft.wakame.ability2.meta.AbilityMeta
import cc.mewcraft.wakame.ability2.trigger.AbilityTriggerVariant
import cc.mewcraft.wakame.ecs.Fleks
import cc.mewcraft.wakame.ecs.bridge.KoishEntity
import cc.mewcraft.wakame.ecs.bridge.koishify
import cc.mewcraft.wakame.ecs.component.TickCountComponent
import cc.mewcraft.wakame.item2.config.property.impl.AbilityOnItem
import cc.mewcraft.wakame.item2.config.property.impl.ItemSlot
import org.bukkit.entity.Player
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
            entity += TickCountComponent(0)
        }

        if (caster.has(AbilityContainer)) {
            val container = caster[AbilityContainer]
            container[metaType] = entity
        }
    }

    fun getPlayerAllSingleAbilities(player: Player): List<AbilityInfo> {
        return player.koishify()[AbilityContainer].convertToSingleAbilityList()
    }
}