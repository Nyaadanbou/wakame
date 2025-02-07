package cc.mewcraft.wakame.command.command

import cc.mewcraft.wakame.ability.Ability
import cc.mewcraft.wakame.ability.character.CasterAdapter
import cc.mewcraft.wakame.ability.character.TargetAdapter
import cc.mewcraft.wakame.ability.context.abilityInput
import cc.mewcraft.wakame.command.CommandPermissions
import cc.mewcraft.wakame.command.KoishCommandFactory
import cc.mewcraft.wakame.command.koishHandler
import cc.mewcraft.wakame.command.parser.AbilityParser
import org.bukkit.Location
import org.bukkit.command.CommandSender
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.incendo.cloud.bukkit.data.SingleEntitySelector
import org.incendo.cloud.bukkit.data.SinglePlayerSelector
import org.incendo.cloud.bukkit.parser.location.LocationParser
import org.incendo.cloud.bukkit.parser.selector.SingleEntitySelectorParser
import org.incendo.cloud.bukkit.parser.selector.SinglePlayerSelectorParser
import org.incendo.cloud.context.CommandContext
import org.incendo.cloud.description.Description
import kotlin.jvm.optionals.getOrNull

internal object AbilityCommand : KoishCommandFactory<CommandSender> {

    override fun KoishCommandFactory.Builder<CommandSender>.createCommands() {
        val commonBuilder = build {
            permission(CommandPermissions.ABILITY)
            literal("ability")
        }

        buildAndAdd(commonBuilder) {
            literal("cast")
            flag("target_entity", arrayOf("e"), Description.of("The target entity of the ability"), SingleEntitySelectorParser.singleEntitySelectorParser())
            flag("target_location", arrayOf("l"), Description.of("The target location of the ability"), LocationParser.locationParser())
            required("ability", AbilityParser.abilityParser())
            optional("caster", SinglePlayerSelectorParser.singlePlayerSelectorParser())
            koishHandler(handler = ::handleCastAbilityAtTarget)
        }
    }

    private fun handleCastAbilityAtTarget(context: CommandContext<CommandSender>) {
        val casterPlayer = context.optional<SinglePlayerSelector>("caster").getOrNull()?.single() ?: context.sender() as? Player ?: return
        val targetEntity = context.flags().get<SingleEntitySelector>("target_entity")?.single() as? LivingEntity
        val targetLocation = context.flags().get<Location>("target_location")
        val target = targetEntity?.let { TargetAdapter.adapt(it) } ?: targetLocation?.let { TargetAdapter.adapt(it) }
        val ability = context.get<Ability>("ability")
        val input = abilityInput(CasterAdapter.adapt(casterPlayer)) { target?.let { target(it) } }
        ability.recordBy(input)
    }

}