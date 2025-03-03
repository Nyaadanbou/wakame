package cc.mewcraft.wakame.command.command

import cc.mewcraft.wakame.ability.Ability
import cc.mewcraft.wakame.ability.character.CasterAdapter
import cc.mewcraft.wakame.ability.character.EmptyCaster
import cc.mewcraft.wakame.ability.character.TargetAdapter
import cc.mewcraft.wakame.ability.context.abilityInput
import cc.mewcraft.wakame.command.CommandPermissions
import cc.mewcraft.wakame.command.KoishCommandFactory
import cc.mewcraft.wakame.command.koishHandler
import cc.mewcraft.wakame.command.parser.AbilityParser
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.incendo.cloud.bukkit.data.SingleEntitySelector
import org.incendo.cloud.bukkit.parser.location.LocationParser
import org.incendo.cloud.bukkit.parser.selector.SingleEntitySelectorParser
import org.incendo.cloud.bukkit.parser.selector.SinglePlayerSelectorParser
import org.incendo.cloud.context.CommandContext
import org.incendo.cloud.description.Description
import org.incendo.cloud.paper.util.sender.Source

internal object AbilityCommand : KoishCommandFactory<Source> {

    override fun KoishCommandFactory.Builder<Source>.createCommands() {
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

    private fun handleCastAbilityAtTarget(context: CommandContext<Source>) {
        val ability = context.get<Ability>("ability")
        val casterPlayer = context.sender() as? Player
        val targetEntity = context.get<SingleEntitySelector>("target").single() as? LivingEntity ?: return
        val target = targetEntity.let { TargetAdapter.adapt(it) }
        val caster = casterPlayer?.let { CasterAdapter.adapt(it) } ?: EmptyCaster
        val input = abilityInput(caster, target)
        ability.recordBy(input)
    }

}