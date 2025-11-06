package cc.mewcraft.wakame.hook.impl.betonquest.quest.item

import cc.mewcraft.wakame.integration.playerlevel.PlayerLevelIntegration
import cc.mewcraft.wakame.item.KoishItem
import cc.mewcraft.wakame.item.KoishStackGenerator
import cc.mewcraft.wakame.item.datagen.ItemGenerationContext
import org.betonquest.betonquest.api.instruction.variable.Variable
import org.betonquest.betonquest.api.profile.Profile
import org.betonquest.betonquest.item.QuestItem
import org.betonquest.betonquest.item.QuestItemWrapper

class KoishQuestItemWrapper(
    private val itemType: Variable<KoishItem>,
) : QuestItemWrapper {

    override fun getItem(profile: Profile?): QuestItem {
        val itemType = itemType.getValue(profile)
        val itemstack = if (profile == null) {
            val ctx = ItemGenerationContext(itemType, 0f, 0)
            KoishStackGenerator.generate(itemType, ctx)
        } else {
            val level = PlayerLevelIntegration.get(profile.playerUUID) ?: 0
            val ctx = ItemGenerationContext(itemType, 0f, level)
            KoishStackGenerator.generate(itemType, ctx)
        }
        return KoishQuestItem(itemstack, itemType)
    }
}