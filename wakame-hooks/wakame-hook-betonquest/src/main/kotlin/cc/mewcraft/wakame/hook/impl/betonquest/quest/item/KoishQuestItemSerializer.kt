package cc.mewcraft.wakame.hook.impl.betonquest.quest.item

import cc.mewcraft.wakame.item.koishTypeId
import org.betonquest.betonquest.item.QuestItemSerializer
import org.bukkit.inventory.ItemStack

class KoishQuestItemSerializer : QuestItemSerializer {

    override fun serialize(itemstack: ItemStack): String {
        val typeId = itemstack.koishTypeId ?: throw IllegalArgumentException("ItemStack is not a exact Koish item")
        return typeId.value() // 不包含命名空间, 因为都是统一的 "koish"
    }
}