package cc.mewcraft.wakame.hook.impl.betonquest.quest.action.party

import cc.mewcraft.wakame.integration.party.PartyIntegration
import cc.mewcraft.wakame.util.adventure.plain
import net.kyori.adventure.text.Component
import org.betonquest.betonquest.api.instruction.Argument
import org.betonquest.betonquest.api.instruction.Instruction
import org.betonquest.betonquest.api.logger.BetonQuestLogger
import org.betonquest.betonquest.api.logger.BetonQuestLoggerFactory
import org.betonquest.betonquest.api.profile.OnlineProfile
import org.betonquest.betonquest.api.profile.ProfileProvider
import org.betonquest.betonquest.api.quest.QuestTypeApi
import org.betonquest.betonquest.api.quest.action.PlayerAction
import org.betonquest.betonquest.api.quest.action.PlayerActionFactory
import org.betonquest.betonquest.api.quest.action.online.OnlineAction
import org.betonquest.betonquest.api.quest.action.online.OnlineActionAdapter
import org.betonquest.betonquest.api.quest.condition.ConditionID
import org.bukkit.Bukkit
import org.bukkit.Location
import kotlin.jvm.optionals.getOrNull


/**
 * @param questTypeApi the Quest Type API
 * @param profileProvider the profile provider instance
 * @param range the range of the party
 * @param amount the optional maximum amount of players affected by this action
 * @param conditions the conditions that must be met by the party members
 */
class CreatePartyAction(
    private val questTypeApi: QuestTypeApi,
    private val profileProvider: ProfileProvider,
    private val range: Argument<Number>,
    private val conditions: Argument<List<ConditionID>>,
    private val amount: Argument<Number>?,
    private val logger: BetonQuestLogger,
) : OnlineAction {

    override fun execute(profile: OnlineProfile) {
        // 使当前玩家离开当前队伍
        val oldParty = PartyIntegration.lookupPartyByPlayer(profile.playerUUID).join()
        if (oldParty != null) {
            oldParty.removeMember(profile.playerUUID)
            logger.info("Removed player ${profile.player.name} from party \"${oldParty.name.plain}\"")
        }

        // 使其他成员离开当前队伍
        val memberProfiles = getMemberList(profile)
        for (memberProfile in memberProfiles) {
            val player = memberProfile.player
            val oldParty = PartyIntegration.lookupPartyByPlayer(memberProfile.playerUUID).join()
            if (oldParty != null) {
                oldParty.removeMember(memberProfile.playerUUID)
                logger.info("Removed player ${player.name} from party \"${oldParty.name.plain}\"")
            }
        }

        // 创建新的队伍
        val partyName = "${profile.player.name}的小队"
        val newParty = PartyIntegration.createParty(Component.text(partyName))

        // 向队伍添加成员
        newParty.addMember(profile.playerUUID)
        for (memberProfile in memberProfiles) {
            newParty.addMember(memberProfile.playerUUID)
        }

        logger.info("Created new party \"$partyName\" with members: [ ${newParty.members.joinToString { Bukkit.getServer().getPlayer(it)?.name ?: "<Not Online>" }} ]")
    }

    private fun getMemberList(profile: OnlineProfile): Set<OnlineProfile> {
        val toExecute = amount?.getValue(profile)?.toInt() ?: -1
        val members = getParty(
            questTypeApi,
            profileProvider.onlineProfiles,
            profile.player.location,
            range.getValue(profile).toDouble(),
            conditions.getValue(profile),
        )

        if (toExecute < 0) {
            return members.keys
        }

        return members.entries
            .asSequence()
            .sortedBy { it.value }
            .take(toExecute)
            .map { it.key }
            .toSet()
    }

    private fun getParty(
        questTypeApi: QuestTypeApi,
        profiles: Collection<OnlineProfile>,
        location: Location,
        range: Double,
        conditions: List<ConditionID>,
    ): Map<OnlineProfile, Double> {
        val world = location.world
        val squared = range * range

        val players = profiles.asSequence()
        val playersInSameWorld = if (range == -1.0) players else players.filter { profile -> profile.player.world == world }
        val playerToDistance = playersInSameWorld.map { profile -> Pair(profile, getDistanceSquared(profile, location)) }
        val rangePlayers = if (range <= 0.0) playerToDistance else playerToDistance.filter { (_, distance) -> distance <= squared }

        return rangePlayers
            .filter { (profile, _) -> questTypeApi.conditions(profile, conditions) }
            .toMap()

    }

    private fun getDistanceSquared(profile: OnlineProfile, location: Location): Double {
        return try {
            profile.player.location.distance(location)
        } catch (_: IllegalArgumentException) {
            Double.MAX_VALUE
        }
    }
}


/**
 * @param loggerFactory the logger factory to create a logger for the actions
 * @param questTypeApi the Quest Type API
 * @param profileProvider the profile provider instance
 */
class CreatePartyActionFactory(
    private val loggerFactory: BetonQuestLoggerFactory,
    private val questTypeApi: QuestTypeApi,
    private val profileProvider: ProfileProvider,
) : PlayerActionFactory {

    override fun parsePlayer(instruction: Instruction): PlayerAction {
        val range = instruction.number().get()
        val conditions = instruction.parse(::ConditionID).list().get()
        val amount = instruction.number().get("amount").getOrNull()
        val logger = loggerFactory.create(CreatePartyAction::class.java)
        val questPackage = instruction.getPackage()
        val action = CreatePartyAction(questTypeApi, profileProvider, range, conditions, amount, logger)
        val adapter = OnlineActionAdapter(action, logger, questPackage)
        return adapter
    }
}