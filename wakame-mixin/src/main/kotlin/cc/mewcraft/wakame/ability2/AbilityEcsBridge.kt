package cc.mewcraft.wakame.ability2

import cc.mewcraft.wakame.ability2.component.Ability
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
import cc.mewcraft.wakame.ecs.component.TickCount
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
        val meta = ability.meta.unwrap()
        val entity = Fleks.INSTANCE.createEntity { entity ->
            entity += Ability(
                meta = meta,
                phase = phase,
                trigger = ability.trigger,
                variant = ability.variant,
                mochaEngine = MochaEngine.createStandard()
            )
            entity += listOf(meta.params)
            entity += CastBy(caster.unwrap())
            entity += TargetTo(target.unwrap())
            ability.manaCost?.let { entity += ManaCost(it) }
            slot?.let { entity += AtSlot(it) }
            entity += TickCount(0)
        }

        if (caster.has(AbilityContainer)) {
            val metaType = meta.type
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
        val entity = Fleks.INSTANCE.createEntity { entity ->
            entity += Ability(
                meta = abilityMeta,
                phase = phase,
                trigger = null,
                variant = AbilityTriggerVariant.any(),
                mochaEngine = MochaEngine.createStandard()
            )
            entity += listOf(abilityMeta.params)
            entity += CastBy(caster.unwrap())
            entity += TargetTo(target.unwrap())
            entity += TickCount(0)
        }

        if (caster.has(AbilityContainer)) {
            val metaType = abilityMeta.type
            val container = caster[AbilityContainer]
            container[metaType] = entity
        }
    }

    fun getPlayerAllSingleAbilities(player: Player): List<Ability> {
        return player.koishify()[AbilityContainer].convertToSingleAbilityList()
    }
}