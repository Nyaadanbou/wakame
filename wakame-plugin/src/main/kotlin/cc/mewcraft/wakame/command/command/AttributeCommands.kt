package cc.mewcraft.wakame.command.command

import cc.mewcraft.wakame.attribute.*
import cc.mewcraft.wakame.command.CommandConstants
import cc.mewcraft.wakame.command.CommandPermissions
import cc.mewcraft.wakame.command.buildAndAdd
import net.kyori.adventure.extra.kotlin.text
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.command.CommandSender
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BookMeta
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

private const val ATTRIBUTE_PER_PAGE = 14

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
                    name = "print_book",
                    description = Description.of("Give the result to the sender as a book")
                )
                suspendingHandler { context ->
                    val sender = context.sender()
                    val isPrintBook = context.flags().contains("print_book")
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

                        val attributeMessage = generateAttributeMessage(recipient, attributeMapSnapshot)
                        sender.sendMessage(attributeMessage)

                        if (isPrintBook && sender is InventoryHolder) {
                            val resultItem = ItemStack.of(Material.WRITTEN_BOOK)
                            resultItem.editMeta { meta ->
                                meta as BookMeta
                                val displayName = text {
                                    content("Attribute map for ")
                                    append(recipient.name())
                                    appendSpace()
                                    append {
                                        Component.text("Generated at ${getCurrentTime()}")
                                            .color(NamedTextColor.GRAY)
                                    }
                                }

                                meta.itemName(displayName)
                                meta.author(sender.name())
                                meta.pages(generateBookPages(attributeMapSnapshot))
                            }

                            sender.inventory.addItem(resultItem)
                        }
                    }
                }
            }.buildAndAdd(this)
        }
    }

    private fun generateAttributeMessage(recipient: Entity, attributeMapSnapShot: Set<Map.Entry<Attribute, AttributeInstanceSnapshot>>): Component {
        return text {
            text {
                content("Attribute map for ")
                append(recipient.name())
                hoverEvent {
                    val hoverText = text {
                        for ((attribute, instance) in attributeMapSnapShot) {
                            append {
                                Component.text(getAttributeId(attribute) + ": ")
                                    .color(NamedTextColor.GREEN)
                            }
                            appendNewline()
                            append(generateAttributeInstanceText(instance))
                        }
                    }
                    @Suppress("UNCHECKED_CAST")
                    HoverEvent.showText(hoverText) as HoverEvent<Any>
                }
            }
        }
    }

    private fun generateAttributeInstanceText(attributeInstanceSnapshot: AttributeInstanceSnapshot): Component {
        return text {
            append {
                Component.text("  Base: ${attributeInstanceSnapshot.getBaseValue()}")
                    .color(NamedTextColor.WHITE)
            }
            for (modifier in attributeInstanceSnapshot.getModifiers()) {
                appendNewline()
                append {
                    Component.text("  ${modifier.operation} ${modifier.amount} from ${modifier.id}")
                        .color(NamedTextColor.GRAY)
                }
            }
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

    private fun generateBookPages(attributeMapSnapShot: Set<Map.Entry<Attribute, AttributeInstanceSnapshot>>): List<Component> {
        val result = mutableListOf<Component>()
        val pages = attributeMapSnapShot.chunked(ATTRIBUTE_PER_PAGE)
        for (page in pages) {
            result.add(generateBookPage(page))
        }
        return result
    }

    private fun generateBookPage(attributeMapSnapShot: List<Map.Entry<Attribute, AttributeInstanceSnapshot>>): Component {
        return text {
            for ((attribute, instance) in attributeMapSnapShot) {
                append {
                    if (getAttributeId(attribute).length > 20) {
                        Component.text(getAttributeId(attribute).substring(0, 17) + "...")
                            .color(NamedTextColor.DARK_GREEN)
                    } else {
                        Component.text(getAttributeId(attribute))
                            .color(NamedTextColor.DARK_GREEN)
                    }.hoverEvent {
                        val hoverText = text {
                            append(Component.text("${getAttributeId(attribute)}: ").color(NamedTextColor.GREEN))
                            appendNewline()
                            append(generateAttributeInstanceText(instance))
                        }
                        @Suppress("UNCHECKED_CAST")
                        HoverEvent.showText(hoverText) as HoverEvent<Any>
                    }
                }
                appendNewline()
            }
        }
    }
}