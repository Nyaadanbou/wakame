package cc.mewcraft.wakame.command.command

import cc.mewcraft.wakame.command.CommandConstants
import cc.mewcraft.wakame.command.CommandPermissions
import cc.mewcraft.wakame.command.buildAndAdd
import cc.mewcraft.wakame.command.parser.SkillParser
import cc.mewcraft.wakame.skill2.MechanicWorldInteraction
import cc.mewcraft.wakame.skill2.Skill
import cc.mewcraft.wakame.skill2.character.CasterAdapter
import cc.mewcraft.wakame.skill2.character.TargetAdapter
import cc.mewcraft.wakame.skill2.character.toComposite
import cc.mewcraft.wakame.skill2.context.skillContext
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
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.jvm.optionals.getOrNull

object SkillCommands : CommandFactory<CommandSender>, KoinComponent {
    private val mechanicWorldInteraction: MechanicWorldInteraction by inject()

    private const val SKILL_LITERAL = "skill"

    override fun createCommands(commandManager: CommandManager<CommandSender>): List<Command<out CommandSender>> {
        return buildList {
            // /<root> skill cast <skill> [player]
            commandManager.commandBuilder(
                name = CommandConstants.ROOT_COMMAND,
                description = Description.of("Commands of skill")
            ) {
                permission(CommandPermissions.SKILL)
                literal(SKILL_LITERAL)
                literal("cast")
                flag(
                    name = "target_entity",
                    description = Description.of("The target entity of the skill"),
                    aliases = arrayOf("e"),
                    parser = SingleEntitySelectorParser.singleEntitySelectorParser()
                )
                flag(
                    name = "target_location",
                    description = Description.of("The target location of the skill"),
                    aliases = arrayOf("l"),
                    parser = LocationParser.locationParser()
                )
                required("skill", SkillParser.skillParser())
                optional("caster", SinglePlayerSelectorParser.singlePlayerSelectorParser())
                handler { context ->
                    val casterPlayer = context.optional<SinglePlayerSelector>("caster").getOrNull()?.single()
                        ?: context.sender() as? Player
                        ?: return@handler
                    val targetEntity = context.flags().get<SingleEntitySelector>("target_entity")?.single() as? LivingEntity
                    val targetLocation = context.flags().get<Location>("target_location")

                    val target = targetEntity?.let { TargetAdapter.adapt(it) }
                        ?: targetLocation?.let { TargetAdapter.adapt(it) }

                    val skill = context.get<Skill>("skill")
                    val context = skillContext(skill, CasterAdapter.adapt(casterPlayer).toComposite()) {
                        target(target)
                    }
                    mechanicWorldInteraction.addMechanic(context)
                }
            }.buildAndAdd(this)
        }
    }
}