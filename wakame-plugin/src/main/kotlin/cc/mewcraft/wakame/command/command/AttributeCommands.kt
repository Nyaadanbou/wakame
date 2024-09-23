package cc.mewcraft.wakame.command.command

import cc.mewcraft.wakame.attribute.ElementAttribute
import cc.mewcraft.wakame.attribute.PlayerAttributeAccessor
import cc.mewcraft.wakame.command.CommandConstants
import cc.mewcraft.wakame.command.CommandPermissions
import cc.mewcraft.wakame.command.buildAndAdd
import net.kyori.adventure.extra.kotlin.text
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.command.CommandSender
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.incendo.cloud.Command
import org.incendo.cloud.CommandFactory
import org.incendo.cloud.CommandManager
import org.incendo.cloud.bukkit.data.MultipleEntitySelector
import org.incendo.cloud.bukkit.parser.selector.MultipleEntitySelectorParser
import org.incendo.cloud.description.Description
import org.incendo.cloud.kotlin.coroutines.extension.suspendingHandler
import org.incendo.cloud.kotlin.extension.commandBuilder
import org.incendo.cloud.kotlin.extension.getOrNull

object AttributeCommands : CommandFactory<CommandSender> {
    private const val ATTRIBUTE_LITERAL = "attribute"

    override fun createCommands(commandManager: CommandManager<CommandSender>): List<Command<out CommandSender>> {
        return buildList {
            // /<root> attribute [entity]
            commandManager.commandBuilder(
                name = CommandConstants.ROOT_COMMAND,
                description = Description.of("Show the entity(s) attributes")
            ) {
                permission(CommandPermissions.ATTRIBUTE)
                literal(ATTRIBUTE_LITERAL)
                optional("entity", MultipleEntitySelectorParser.multipleEntitySelectorParser())
                suspendingHandler { context ->
                    val sender = context.sender()
                    val recipients = context.getOrNull<MultipleEntitySelector>("entity")?.values()
                        ?: (sender as? Player)?.let(::listOf)
                        ?: emptyList()

                    for (recipient in recipients) {
                        val attributeMapSnapshot = when (recipient) {
                            is Player -> PlayerAttributeAccessor.getAttributeMap(recipient).getSnapshot()
                            is LivingEntity -> null // EntityAttributeAccessor.getAttributeMap(recipient).getSnapshot()
                            else -> null
                        }
                        if (attributeMapSnapshot == null) {
                            sender.sendMessage(
                                Component.text("No attribute map found for ${recipient.name()}")
                            )
                            continue
                        }

                        val attributeMessage = text {
                            content("Attribute map for ")
                            append(recipient.name())
                            hoverEvent {
                                val hoverMessage = Component.text()
                                for ((attribute, instances) in attributeMapSnapshot) {
                                    hoverMessage
                                        .append {
                                            if (attribute is ElementAttribute) {
                                                val element = attribute.element
                                                Component.text("${attribute.descriptionId}/${element.uniqueId}: \n")
                                                    .color(NamedTextColor.GOLD)
                                            } else {
                                                Component.text("${attribute.descriptionId}: \n")
                                                    .color(NamedTextColor.GREEN)
                                            }
                                        }
                                        .append {
                                            Component.text("  Base: ${instances.getBaseValue()}\n")
                                                .color(NamedTextColor.WHITE)
                                        }
                                    for (modifier in instances) {
                                        hoverMessage.append {
                                            Component.text("  ${modifier.operation} ${modifier.amount} from ${modifier.id}\n")
                                                .color(NamedTextColor.GRAY)
                                        }
                                    }
                                }
                                @Suppress("UNCHECKED_CAST")
                                HoverEvent.showText(hoverMessage.build()) as HoverEvent<Any>
                            }
                        }

                        sender.sendMessage(attributeMessage)
                    }
                }
            }.buildAndAdd(this)
        }
    }
}