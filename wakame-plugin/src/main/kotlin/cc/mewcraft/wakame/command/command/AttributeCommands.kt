package cc.mewcraft.wakame.command.command

import cc.mewcraft.wakame.attribute.Attribute
import cc.mewcraft.wakame.attribute.ElementAttribute
import cc.mewcraft.wakame.attribute.PlayerAttributeAccessor
import cc.mewcraft.wakame.command.CommandConstants
import cc.mewcraft.wakame.command.CommandPermissions
import cc.mewcraft.wakame.command.buildAndAdd
import cc.mewcraft.wakame.util.removeItalic
import net.kyori.adventure.extra.kotlin.text
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.command.CommandSender
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack
import org.incendo.cloud.Command
import org.incendo.cloud.CommandFactory
import org.incendo.cloud.CommandManager
import org.incendo.cloud.bukkit.data.MultipleEntitySelector
import org.incendo.cloud.bukkit.parser.selector.MultipleEntitySelectorParser
import org.incendo.cloud.description.Description
import org.incendo.cloud.kotlin.coroutines.extension.suspendingHandler
import org.incendo.cloud.kotlin.extension.commandBuilder
import org.incendo.cloud.kotlin.extension.getOrNull
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object AttributeCommands : CommandFactory<CommandSender> {
    private const val ATTRIBUTE_LITERAL = "attribute"

    override fun createCommands(commandManager: CommandManager<CommandSender>): List<Command<out CommandSender>> {
        return buildList {
            // /<root> attribute [entity] [--result_paper]
            commandManager.commandBuilder(
                name = CommandConstants.ROOT_COMMAND,
                description = Description.of("Show the entity(s) attributes")
            ) {
                permission(CommandPermissions.ATTRIBUTE)
                literal(ATTRIBUTE_LITERAL)
                optional("entity", MultipleEntitySelectorParser.multipleEntitySelectorParser())
                flag(
                    name = "result_paper",
                    description = Description.of("Give the result to the sender as a paper")
                )
                suspendingHandler { context ->
                    val sender = context.sender()
                    val isResultPaper = context.flags().contains("result_paper")
                    val recipients = context.getOrNull<MultipleEntitySelector>("entity")?.values()
                        ?: (sender as? Player)?.let(::listOf)
                        ?: emptyList()

                    for (recipient in recipients) {
                        val attributeMapSnapshot = when (recipient) {
                            is Player -> PlayerAttributeAccessor.getAttributeMap(recipient).getSnapshot()
                            is LivingEntity -> null // EntityAttributeAccessor.getAttributeMap(recipient).getSnapshot()
                            else -> null
                        }?.toSortedSet(compareBy { getAttributeId(it.key) })
                        if (attributeMapSnapshot == null) {
                            sender.sendMessage(
                                Component.text("No attribute map found for ${recipient.name()}")
                            )
                            continue
                        }

                        val resultMessage = ArrayList<Component>()
                        for ((attribute, instances) in attributeMapSnapshot) {
                            resultMessage.add(
                                Component.text(getAttributeId(attribute) + ": ")
                                    .color(NamedTextColor.GREEN)
                            )
                            resultMessage.add(
                                Component.text("  Base: ${instances.getBaseValue()}")
                                    .color(NamedTextColor.WHITE)
                            )
                            for (modifier in instances) {
                                resultMessage.add(
                                    Component.text("  ${modifier.operation} ${modifier.amount} from ${modifier.id}")
                                        .color(NamedTextColor.GRAY)
                                )
                            }
                        }

                        val attributeMessage = text {
                            content("Attribute map for ")
                            append(recipient.name())
                            hoverEvent {
                                val hoverText = Component.text()
                                for (message in resultMessage) {
                                    hoverText.append(message)
                                        .appendNewline()
                                }
                                @Suppress("UNCHECKED_CAST")
                                HoverEvent.showText(hoverText) as HoverEvent<Any>
                            }
                        }

                        sender.sendMessage(attributeMessage)

                        if (isResultPaper && sender is InventoryHolder) {
                            val resultItem = ItemStack.of(Material.PAPER)
                            resultItem.editMeta { meta ->
                                val displayName = text {
                                    content("Attribute map for ")
                                    append(recipient.name())
                                    appendSpace()
                                    append {
                                        Component.text("Generated at ${getCurrentTime()}")
                                            .color(NamedTextColor.GRAY)
                                    }
                                }.removeItalic

                                meta.displayName(displayName)
                                meta.lore(resultMessage.map(Component::removeItalic))
                            }

                            sender.inventory.addItem(resultItem)
                        }
                    }
                }
            }.buildAndAdd(this)
        }
    }

    private fun getAttributeId(attribute: Attribute): String {
        return when (attribute) {
            is ElementAttribute -> "${attribute.descriptionId}/${attribute.element.uniqueId}"
            else -> attribute.descriptionId
        }
    }

    private fun getCurrentTime(): String {
        val currentTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        return currentTime.format(formatter)
    }
}