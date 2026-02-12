package cc.mewcraft.wakame.hook.impl.betonquest.quest.action.koish

import cc.mewcraft.wakame.item.data.ItemDataTypes
import cc.mewcraft.wakame.item.getProp
import cc.mewcraft.wakame.item.hasData
import cc.mewcraft.wakame.item.property.ItemPropTypes
import cc.mewcraft.wakame.item.setData
import org.betonquest.betonquest.api.instruction.Instruction
import org.betonquest.betonquest.api.logger.BetonQuestLogger
import org.betonquest.betonquest.api.logger.BetonQuestLoggerFactory
import org.betonquest.betonquest.api.profile.OnlineProfile
import org.betonquest.betonquest.api.quest.action.OnlineAction
import org.betonquest.betonquest.api.quest.action.OnlineActionAdapter
import org.betonquest.betonquest.api.quest.action.PlayerAction
import org.betonquest.betonquest.api.quest.action.PlayerActionFactory
import org.bukkit.NamespacedKey
import org.bukkit.persistence.PersistentDataType


class ReplaceCrateKeyAction(
    private val logger: BetonQuestLogger,
) : OnlineAction {

    companion object {
        // 这个 PDC_KEY 的值需要根据 ExcellentCrates 的具体实现随时调整:
        // https://github.com/nulli0n/ExcellentCrates-spigot/blob/master/src/main/java/su/nightexpress/excellentcrates/config/Keys.java#L10
        private const val PDC_KEY = "excellentcrates:crate_key.id"
    }

    override fun execute(profile: OnlineProfile) {
        val player = profile.player
        for (itemstack in player.inventory) {
            if (itemstack == null || itemstack.isEmpty) continue
            val crateKeyRepl = itemstack.getProp(ItemPropTypes.CRATE_KEY_REPLACEMENT) ?: continue
            if (itemstack.hasData(ItemDataTypes.CRATE_KEY_REPLACED)) continue
            itemstack.setData<Unit>(ItemDataTypes.CRATE_KEY_REPLACED)
            itemstack.editPersistentDataContainer { pdc ->
                pdc.set(NamespacedKey.fromString(PDC_KEY) ?: error("Failed to create NamespaceKey from: '$PDC_KEY'"), PersistentDataType.STRING, crateKeyRepl)
            }
        }
    }
}


class ReplaceCrateKeyActionFactory(
    private val loggerFactory: BetonQuestLoggerFactory,
) : PlayerActionFactory {

    override fun parsePlayer(instruction: Instruction): PlayerAction {
        val logger = loggerFactory.create(ReplaceCrateKeyAction::class.java)
        val action = ReplaceCrateKeyAction(logger)
        val questPackage = instruction.getPackage()
        val adapter = OnlineActionAdapter(action, logger, questPackage)
        return adapter
    }
}