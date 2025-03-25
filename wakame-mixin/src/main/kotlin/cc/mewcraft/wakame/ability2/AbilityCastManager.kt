package cc.mewcraft.wakame.ability2

import cc.mewcraft.wakame.ability2.data.StatePhase
import cc.mewcraft.wakame.ability2.meta.AbilityMeta
import cc.mewcraft.wakame.ecs.bridge.KoishEntity
import cc.mewcraft.wakame.ecs.bridge.koishify
import cc.mewcraft.wakame.item.ItemSlot
import org.bukkit.entity.Entity
import org.bukkit.entity.Player

object AbilityCastManager {
    fun record(
        ability: AbilityObject,
        caster: Player,
        target: Entity,
        slot: ItemSlot,
    ) {
        val target = getKoishfiedLivingEntity(target)
        AbilityEcsBridge.createEcsEntities(ability, caster.koishify(), target, StatePhase.IDLE, slot)
    }

    fun castMeta(
        ability: AbilityMeta,
        caster: Entity,
        target: Entity,
    ) {
        val caster = getKoishfiedLivingEntity(caster)
        val target = getKoishfiedLivingEntity(target)

        AbilityEcsBridge.createEcsEntities(ability, caster, target, StatePhase.CAST_POINT)
    }

    fun castObject(
        ability: AbilityObject,
        caster: Entity,
        target: Entity,
    ) {
        val caster = getKoishfiedLivingEntity(caster)
        val target = getKoishfiedLivingEntity(target)

        AbilityEcsBridge.createEcsEntities(ability, caster, target, StatePhase.CAST_POINT, null)
    }

    private fun getKoishfiedLivingEntity(entity: Entity): KoishEntity {
        return if (entity is Player) {
            entity.koishify()
        } else {
            entity.koishify()
        }
    }
}