package cc.mewcraft.wakame.command.command

import cc.mewcraft.wakame.command.CommandConstants
import cc.mewcraft.wakame.command.CommandPermissions
import cc.mewcraft.wakame.command.buildAndAdd
import cc.mewcraft.wakame.command.parser.AbilityParser
import cc.mewcraft.wakame.ability.Ability
import cc.mewcraft.wakame.ability.character.CasterAdapter
import cc.mewcraft.wakame.ability.character.TargetAdapter
import cc.mewcraft.wakame.ability.context.abilityInput
import org.bukkit.Location
import org.bukkit.command.CommandSender
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.incendo.cloud.Command
import org.incendo.cloud.CommandFactory
import org.incendo.cloud.CommandManager
import org.incendo.cloud.bukkit.data.SingleEntitySelector
import org.incendo.cloud.bukkit.data.SinglePlayerSelector
import org.incendo.cloud.bukkit.parser.location.LocationParser
import org.incendo.cloud.bukkit.parser.selector.SingleEntitySelectorParser
import org.incendo.cloud.bukkit.parser.selector.SinglePlayerSelectorParser
import org.incendo.cloud.description.Description
import org.incendo.cloud.kotlin.extension.commandBuilder
import kotlin.jvm.optionals.getOrNull

object AbilityCommands : CommandFactory<CommandSender> {
    private const val ABILITY_LITERAL = "ability"

    override fun createCommands(commandManager: CommandManager<CommandSender>): List<Command<out CommandSender>> {
        return buildList {
            // /<root> ability cast <ability> [player]
            commandManager.commandBuilder(
                name = CommandConstants.ROOT_COMMAND,
                description = Description.of("Commands of ability")
            ) {
                permission(CommandPermissions.ABILITY)
                literal(ABILITY_LITERAL)
                literal("cast")
                flag(
                    name = "target_entity",
                    description = Description.of("The target entity of the ability"),
                    aliases = arrayOf("e"),
                    parser = SingleEntitySelectorParser.singleEntitySelectorParser()
                )
                flag(
                    name = "target_location",
                    description = Description.of("The target location of the ability"),
                    aliases = arrayOf("l"),
                    parser = LocationParser.locationParser()
                )
                required("ability", AbilityParser.abilityParser())
                optional("caster", SinglePlayerSelectorParser.singlePlayerSelectorParser())
                handler { context ->
                    val casterPlayer = context.optional<SinglePlayerSelector>("caster").getOrNull()?.single()
                        ?: context.sender() as? Player
                        ?: return@handler
                    val targetEntity = context.flags().get<SingleEntitySelector>("target_entity")?.single() as? LivingEntity
                    val targetLocation = context.flags().get<Location>("target_location")

                    val target = targetEntity?.let { TargetAdapter.adapt(it) }
                        ?: targetLocation?.let { TargetAdapter.adapt(it) }

                    val ability = context.get<Ability>("ability")
                    val input = abilityInput(CasterAdapter.adapt(casterPlayer)) {
                        target?.let { target(it) }
                    }
                    ability.recordBy(input)
                }
            }.buildAndAdd(this)
        }
    }
}