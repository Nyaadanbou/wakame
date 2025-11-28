@file:JvmName("KoishItems")

package cc.mewcraft.wakame.hook.impl.betonquest.quest.item

import cc.mewcraft.wakame.integration.playerlevel.PlayerLevelIntegration
import cc.mewcraft.wakame.item.*
import cc.mewcraft.wakame.item.datagen.ItemGenerationContext
import cc.mewcraft.wakame.item.property.ItemPropTypes
import cc.mewcraft.wakame.registry.BuiltInRegistries
import net.kyori.adventure.text.Component
import org.betonquest.betonquest.api.instruction.Instruction
import org.betonquest.betonquest.api.instruction.variable.Variable
import org.betonquest.betonquest.api.kernel.TypeFactory
import org.betonquest.betonquest.api.profile.Profile
import org.betonquest.betonquest.api.quest.QuestException
import org.betonquest.betonquest.item.QuestItem
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

class KoishQuestItemFactory : TypeFactory<QuestItemWrapper> {

    override fun parseInstruction(instruction: Instruction): QuestItemWrapper {
        val itemType = instruction.get<KoishItem> { BuiltInRegistries.ITEM[it] }
        val itemWrapper = KoishQuestItemWrapper(itemType)
        if (instruction.hasArgument("quest-item")) {
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
    private val itemType: Variable<KoishItem>,
) : QuestItemWrapper {

    override fun getItem(profile: Profile?): QuestItem {
        val itemType = itemType.getValue(profile)
        return KoishQuestItem(itemType)
    }
}