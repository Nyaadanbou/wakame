package cc.mewcraft.wakame.hook.impl.betonquest.quest.item

import cc.mewcraft.wakame.integration.playerlevel.PlayerLevelIntegration
import cc.mewcraft.wakame.item.*
import cc.mewcraft.wakame.item.datagen.ItemGenerationContext
import cc.mewcraft.wakame.item.property.ItemPropTypes
import net.kyori.adventure.text.Component
import org.betonquest.betonquest.api.profile.Profile
import org.betonquest.betonquest.item.QuestItem
import org.bukkit.inventory.ItemStack

class KoishQuestItem(
    private val itemType: KoishItem,
) : QuestItem {

    override fun getName(): Component {
        return itemType.name
    }

    override fun getLore(): List<Component> {
        return itemType.getProperty(ItemPropTypes.EXTRA_LORE)?.lore ?: emptyList()
    }

    override fun generate(stackSize: Int, profile: Profile?): ItemStack {
        val itemstack = if (profile == null) {
            val ctx = ItemGenerationContext(itemType, 0f, 0)
            KoishStackGenerator.generate(itemType, ctx)
        } else {
            val level = PlayerLevelIntegration.get(profile.playerUUID) ?: 0
            val ctx = ItemGenerationContext(itemType, 0f, level)
            KoishStackGenerator.generate(itemType, ctx)
        }
        return itemstack
    }

    override fun matches(item: ItemStack?): Boolean {
        return item?.koishTypeId == itemType.id
    }
}