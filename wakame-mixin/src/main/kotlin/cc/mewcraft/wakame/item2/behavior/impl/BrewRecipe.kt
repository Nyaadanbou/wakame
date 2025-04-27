package cc.mewcraft.wakame.item2.behavior.impl

import cc.mewcraft.wakame.event.bukkit.PlayerItemRightClickEvent
import cc.mewcraft.wakame.item2.behavior.ItemBehavior
import cc.mewcraft.wakame.item2.data.ItemDataTypes
import cc.mewcraft.wakame.item2.getData
import cc.mewcraft.wakame.item2.setData
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack

object BrewRecipe : ItemBehavior {

    /**
     * 当玩家手持一个未学习的配方并右键时, 学习该配方.
     *
     * 这里的“学习”指的是修改物品上的酒酿配方数据 - 将其修改为“已学习”, 跟玩家自身的状态没有关系.
     * 也就是说玩家可以无限的学习酒酿配方, 无论他先前有没有学习过这个酒酿配方.
     * 一个带有酒酿配方的物品, 其“已学习”的状态会影响其最终的物品渲染效果.
     */
    override fun handleRightClick(player: Player, itemstack: ItemStack, hand: EquipmentSlot, event: PlayerItemRightClickEvent) {
        val itemBrewRecipe = itemstack.getData(ItemDataTypes.BREW_RECIPE)
        if (itemBrewRecipe == null) return
        if (itemBrewRecipe.learned) return
        if (hand != EquipmentSlot.HAND) return
        itemstack.setData(ItemDataTypes.BREW_RECIPE, itemBrewRecipe.copy(learned = true))
        player.sendMessage("你学习了酒酿配方: ${itemBrewRecipe.recipeId}")
    }
}