package cc.mewcraft.wakame.event.bukkit

import cc.mewcraft.wakame.ability2.meta.AbilityMeta
import cc.mewcraft.wakame.ecs.bridge.KoishEntity
import org.bukkit.entity.Player
import org.bukkit.event.HandlerList
import org.bukkit.inventory.ItemStack

class PlayerAbilityPrepareCastEvent(
    ability: AbilityMeta,
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