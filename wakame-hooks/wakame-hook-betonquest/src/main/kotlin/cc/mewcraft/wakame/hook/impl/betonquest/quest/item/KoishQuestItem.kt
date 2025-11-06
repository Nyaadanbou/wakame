package cc.mewcraft.wakame.hook.impl.betonquest.quest.item

import cc.mewcraft.wakame.item.KoishItem
import cc.mewcraft.wakame.item.getProperty
import cc.mewcraft.wakame.item.koishTypeId
import cc.mewcraft.wakame.item.name
import cc.mewcraft.wakame.item.property.ItemPropTypes
import net.kyori.adventure.text.Component
import org.betonquest.betonquest.api.profile.Profile
import org.betonquest.betonquest.item.QuestItem
import org.bukkit.inventory.ItemStack

class KoishQuestItem(
    private val resolvedItem: ItemStack,
    private val itemType: KoishItem,
) : QuestItem {

    override fun getName(): Component {
        return itemType.name
    }

    override fun getLore(): List<Component> {
        return itemType.getProperty(ItemPropTypes.EXTRA_LORE)?.lore ?: emptyList()
    }

    override fun generate(stackSize: Int, profile: Profile?): ItemStack {
        return resolvedItem.clone()
    }

    override fun matches(item: ItemStack?): Boolean {
        return item?.koishTypeId == itemType.id
    }
}