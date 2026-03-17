package cc.mewcraft.wakame.item.behavior.impl

import cc.mewcraft.wakame.item.behavior.InteractionResult
import cc.mewcraft.wakame.item.behavior.UseContext
import cc.mewcraft.wakame.item.getProp
import cc.mewcraft.wakame.item.property.ItemPropTypes

object VirtualBook : SimpleInteract {

    override fun handleSimpleUse(context: UseContext): InteractionResult {
        val itemstack = context.itemstack
        val virtualBook = itemstack.getProp(ItemPropTypes.VIRTUAL_BOOK)
            ?: return InteractionResult.PASS
        val book = virtualBook.createAdventureBook()
        val player = context.player
        player.openBook(book)
        return InteractionResult.SUCCESS
    }
}