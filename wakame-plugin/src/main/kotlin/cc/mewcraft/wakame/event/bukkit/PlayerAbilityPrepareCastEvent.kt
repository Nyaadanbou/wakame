package cc.mewcraft.wakame.event.bukkit

import cc.mewcraft.wakame.ability.Ability
import cc.mewcraft.wakame.ecs.bridge.KoishEntity
import org.bukkit.entity.Player
import org.bukkit.event.HandlerList
import org.bukkit.inventory.ItemStack

class PlayerAbilityPrepareCastEvent(
    ability: Ability,
    val caster: Player,
    val target: KoishEntity?,
    val item: ItemStack?
) : AbilityPrepareCastEvent(ability) {
    
    override fun getHandlers(): HandlerList {
        return HANDLER_LIST
    }

    companion object {
        @JvmStatic
        val HANDLER_LIST = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList = HANDLER_LIST
    }
}