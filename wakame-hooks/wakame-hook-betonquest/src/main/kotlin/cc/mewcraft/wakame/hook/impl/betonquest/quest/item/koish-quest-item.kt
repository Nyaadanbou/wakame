@file:JvmName("KoishItems")

package cc.mewcraft.wakame.hook.impl.betonquest.quest.item

import cc.mewcraft.wakame.integration.playerlevel.PlayerLevelIntegration
import cc.mewcraft.wakame.item.*
import cc.mewcraft.wakame.item.datagen.ItemGenerationContext
import cc.mewcraft.wakame.item.property.ItemPropTypes
import cc.mewcraft.wakame.registry.BuiltInRegistries
import net.kyori.adventure.text.Component
import org.betonquest.betonquest.api.QuestException
import org.betonquest.betonquest.api.instruction.Argument
import org.betonquest.betonquest.api.instruction.Instruction
import org.betonquest.betonquest.api.item.QuestItem
import org.betonquest.betonquest.api.kernel.TypeFactory
import org.betonquest.betonquest.api.profile.Profile
import org.betonquest.betonquest.item.QuestItemSerializer
import org.betonquest.betonquest.item.QuestItemTagAdapterWrapper
import org.betonquest.betonquest.item.QuestItemWrapper
import org.bukkit.inventory.ItemStack


class KoishQuestItem(
    private val itemType: KoishItem,
) : QuestItem {

    override fun getName(): Component {
        return itemType.name
    }

    override fun getLore(): List<Component> {
        return itemType.getProp(ItemPropTypes.EXTRA_LORE)?.lore ?: emptyList()
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
        return itemstack.apply { amount = stackSize }
    }

    override fun matches(item: ItemStack?): Boolean {
        return item?.koishTypeId == itemType.id
    }
}

class KoishQuestItemFactory : TypeFactory<QuestItemWrapper> {

    override fun parseInstruction(instruction: Instruction): QuestItemWrapper {
        val itemType = instruction.parse<KoishItem> { BuiltInRegistries.ITEM[it] }.get()
        val itemWrapper = KoishQuestItemWrapper(itemType)
        if (instruction.bool().getFlag("quest-item", false).getValue(null).orElse(false)) {
            return QuestItemTagAdapterWrapper(itemWrapper)
        }
        return itemWrapper
    }
}

class KoishQuestItemSerializer : QuestItemSerializer {

    override fun serialize(itemstack: ItemStack): String {
        val typeId = itemstack.koishTypeId ?: throw QuestException("ItemStack is not a exact Koish item")
        return typeId.value() // 不包含命名空间, 因为都是统一的 "koish"
    }
}

class KoishQuestItemWrapper(
    private val itemType: Argument<KoishItem>,
) : QuestItemWrapper {

    override fun getItem(profile: Profile?): QuestItem {
        val itemType = itemType.getValue(profile)
        return KoishQuestItem(itemType)
    }
}