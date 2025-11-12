package cc.mewcraft.wakame.hook.impl.betonquest.quest.item

import cc.mewcraft.wakame.item.KoishItem
import cc.mewcraft.wakame.registry.BuiltInRegistries
import org.betonquest.betonquest.api.instruction.Instruction
import org.betonquest.betonquest.api.kernel.TypeFactory
import org.betonquest.betonquest.item.QuestItemTagAdapterWrapper
import org.betonquest.betonquest.item.QuestItemWrapper

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