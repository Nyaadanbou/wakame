package cc.mewcraft.wakame.hook.impl.betonquest.quest.action.towny

import cc.mewcraft.wakame.integration.towny.GovernmentType
import cc.mewcraft.wakame.integration.towny.TownyLocal
import cc.mewcraft.wakame.util.dialog.DialogUtils
import io.papermc.paper.registry.data.dialog.input.TextDialogInput
import net.kyori.adventure.text.Component
import org.betonquest.betonquest.api.instruction.Argument
import org.betonquest.betonquest.api.instruction.Instruction
import org.betonquest.betonquest.api.logger.BetonQuestLogger
import org.betonquest.betonquest.api.logger.BetonQuestLoggerFactory
import org.betonquest.betonquest.api.profile.OnlineProfile
import org.betonquest.betonquest.api.quest.action.OnlineAction
import org.betonquest.betonquest.api.quest.action.OnlineActionAdapter
import org.betonquest.betonquest.api.quest.action.PlayerAction
import org.betonquest.betonquest.api.quest.action.PlayerActionFactory

class UpdateGovernmentBoard(
    private val logger: BetonQuestLogger,
    private val govType: Argument<GovernmentType>,
) : OnlineAction {

    override fun execute(profile: OnlineProfile) {
        val player = profile.player
        val playerId = profile.playerUUID
        val government = when (govType.getValue(profile)) {
            GovernmentType.TOWN -> TownyLocal.getTown(playerId) ?: return
            GovernmentType.NATION -> TownyLocal.getNation(playerId) ?: return
        }
        DialogUtils.getPlayerTextInput(
            player = player,
            inputWidth = 128,
            inputLabel = Component.text("商铺公告板"),
            inputLabelVisible = true,
            inputInitial = government.board.joinToString("\n"),
            inputMaxLength = 512,
            inputMultilineOptions = TextDialogInput.MultilineOptions.create(16, 128),
        ) { _, input ->
            government.board = input
        }
    }
}

class UpdateGovernmentBoardFactory(
    private val loggerFactory: BetonQuestLoggerFactory,
) : PlayerActionFactory {

    override fun parsePlayer(instruction: Instruction): PlayerAction {
        val logger = loggerFactory.create(UpdateGovernmentBoard::class.java)
        val govType = instruction.enumeration(GovernmentType::class.java).get()
        val adapter = OnlineActionAdapter(UpdateGovernmentBoard(logger, govType))
        return adapter
    }
}