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
import org.bukkit.entity.LivingEntity
import org.incendo.cloud.bukkit.data.SingleEntitySelector
import org.incendo.cloud.bukkit.data.SinglePlayerSelector
import org.incendo.cloud.bukkit.parser.location.LocationParser
import org.incendo.cloud.bukkit.parser.selector.SingleEntitySelectorParser
import org.incendo.cloud.bukkit.parser.selector.SinglePlayerSelectorParser
import org.incendo.cloud.context.CommandContext
import org.incendo.cloud.paper.util.sender.Source
import org.incendo.cloud.parser.standard.EitherParser
import org.incendo.cloud.type.Either

internal object AbilityCommand : KoishCommandFactory<Source> {

    override fun KoishCommandFactory.Builder<Source>.createCommands() {
        val commonBuilder = build {
            permission(CommandPermissions.ABILITY)
            literal("ability")
        }

        buildAndAdd(commonBuilder) {
            literal("cast")
            required("caster", SinglePlayerSelectorParser.singlePlayerSelectorParser())
            required("ability", AbilityParser.abilityParser())
            required("target", EitherParser.eitherParser(SingleEntitySelectorParser.singleEntitySelectorParser(), LocationParser.locationParser()))
            koishHandler(handler = ::handleCastAbilityAtTarget)
        }
    }

    private fun handleCastAbilityAtTarget(context: CommandContext<Source>) {
        val ability = context.get<Ability>("ability")
        val casterPlayer = context.get<SinglePlayerSelector>("caster").single()
        val caster = CasterAdapter.adapt(casterPlayer)
        val target = context.get<Either<SingleEntitySelector, Location>>("target")
            .mapEither(
                { selector ->
                    val livingEntity = selector.single() as LivingEntity
                    TargetAdapter.adapt(livingEntity)
                },
                { location -> TargetAdapter.adapt(location) }
            )
        val input = abilityInput(caster, target)
        ability.recordBy(input)
    }

}