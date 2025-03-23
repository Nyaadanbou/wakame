package cc.mewcraft.wakame.ability2

import cc.mewcraft.wakame.item.ItemSlot
import org.bukkit.entity.Player

object AbilityCaster {
    fun record(
        ability: AbilityObject,
        caster: Player,
        target: KoishEntity,
        slot: ItemSlot,
    ) {
        println("Recorded ability: $ability")
    }

    fun cast(
        ability: AbilityObject,
        caster: Player,
        target: KoishEntity,
    ) {
        println("Cast ability: $ability")
    }
}