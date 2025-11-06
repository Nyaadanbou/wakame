package cc.mewcraft.wakame.hook.impl.betonquest.quest.item

import cc.mewcraft.wakame.item.KoishItem
import org.betonquest.betonquest.api.instruction.variable.Variable
import org.betonquest.betonquest.api.profile.Profile
import org.betonquest.betonquest.item.QuestItem
import org.betonquest.betonquest.item.QuestItemWrapper

class KoishQuestItemWrapper(
    private val itemType: Variable<KoishItem>,
) : QuestItemWrapper {

    override fun getItem(profile: Profile?): QuestItem {
        val itemType = itemType.getValue(profile)
        return KoishQuestItem(itemType)
    }
}