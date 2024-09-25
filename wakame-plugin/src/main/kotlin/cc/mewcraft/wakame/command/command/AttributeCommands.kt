package cc.mewcraft.wakame.command.command

import cc.mewcraft.wakame.attribute.*
import cc.mewcraft.wakame.command.CommandConstants
import cc.mewcraft.wakame.command.CommandPermissions
import cc.mewcraft.wakame.command.buildAndAdd
import cc.mewcraft.wakame.util.editMeta
import net.kyori.adventure.extra.kotlin.text
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
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
import org.incendo.cloud.bukkit.data.SingleEntitySelector
import org.incendo.cloud.bukkit.parser.selector.SingleEntitySelectorParser
import org.incendo.cloud.description.Description
import org.incendo.cloud.kotlin.coroutines.extension.suspendingHandler
import org.incendo.cloud.kotlin.extension.commandBuilder
import org.incendo.cloud.kotlin.extension.getOrNull
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private const val ATTRIBUTE_COUNT_PER_PAGE = 14

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
                optional("entity", SingleEntitySelectorParser.singleEntitySelectorParser())
                flag("print", description = Description.of("Generate a book containing the result to the recipient's inventory"))
                suspendingHandler { context ->
                    val sender = context.sender()
                    val isPrinting = context.flags().contains("print")
                    val recipient = context.getOrNull<SingleEntitySelector>("entity")?.single() ?: (sender as? Player)
                    if (recipient == null) {
                        sender.sendMessage(Component.text("No entity found."))
                        return@suspendingHandler
                    }

                    val attributeMap = when (recipient) {
                        is Player -> PlayerAttributeAccessor.getAttributeMap(recipient).getSnapshot()
                        is LivingEntity -> EntityAttributeAccessor.getAttributeMap(recipient).getSnapshot()
                        else -> null
                    }?.sortedBy { (type, _) -> type.descriptionId }
                    if (attributeMap == null) {
                        val recipientName = recipient.name()
                            .hoverEvent(HoverEvent.showText(Component.text("Click to copy the UUID of this entity")))
                            .clickEvent(ClickEvent.copyToClipboard(recipient.uniqueId.toString()))
                        sender.sendMessage(recipientName.append(Component.text(" has no attributes.")))
                        return@suspendingHandler
                    }

                    val wholeResultText = generateWholeText(recipient, attributeMap)
                    sender.sendMessage(wholeResultText)

                    if (isPrinting && sender is InventoryHolder) {
                        val bookItem = ItemStack.of(Material.WRITTEN_BOOK)
                        bookItem.editMeta<BookMeta> { meta ->
                            meta.itemName(text {
                                content("属性报告之 ")
                                append(recipient.name())
                                append {
                                    text {
                                        content(" 于 ${getCurrentTime()}")
                                        color(NamedTextColor.GRAY)
                                    }
                                }
                            })
                            meta.author(sender.name())
                            meta.pages(generateBookPages(attributeMap))
                        }

                        sender.inventory.addItem(bookItem)
                    }
                }
            }.buildAndAdd(this)
        }
    }

    private fun generateWholeText(recipient: Entity, attributeMap: List<Map.Entry<Attribute, AttributeInstanceSnapshot>>): Component {
        return text {
            content("Attributes of ")
            append(recipient.name())
            appendNewline()
            hoverEvent {
                HoverEvent.showText(text {
                    for ((type, instance) in attributeMap) {
                        append {
                            text {
                                content(type.descriptionId + ": ")
                                color(NamedTextColor.GREEN)
                            }
                        }
                        appendNewline()
                        append(generateAttributeInstanceText(instance))
                    }
                }) as HoverEvent<Any>
            }
        }
    }

    private fun generateAttributeInstanceText(instance: AttributeInstanceSnapshot): Component {
        return text {
            append {
                text {
                    content("  Base: ${instance.getBaseValue()}")
                    color(NamedTextColor.WHITE)
                }
            }
            for (modifier in instance.getModifiers()) {
                appendNewline()
                append {
                    text {
                        content("  ${modifier.operation} ${modifier.amount} from ${modifier.id}")
                        color(NamedTextColor.GRAY)
                    }
                }
            }
        }
    }

    private fun getCurrentTime(): String {
        return LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    }

    private fun generateBookPages(attributeMapSnapShot: List<Map.Entry<Attribute, AttributeInstanceSnapshot>>): List<Component> {
        val result = mutableListOf<Component>()
        val chunked = attributeMapSnapShot.chunked(ATTRIBUTE_COUNT_PER_PAGE)
        for (chunk in chunked) {
            result.add(generateBookPage(chunk))
        }
        return result // 列表中的每个元素代表书的一页
    }

    private fun generateBookPage(attributeList: List<Map.Entry<Attribute, AttributeInstanceSnapshot>>): Component {
        return text {
            for ((type, instance) in attributeList) {
                append {
                    text {
                        if (type.descriptionId.length > 20) {
                            content(type.descriptionId.substring(0, 17) + "...")
                            color(NamedTextColor.DARK_GREEN)
                        } else {
                            content(type.descriptionId)
                            color(NamedTextColor.DARK_GREEN)
                        }
                        hoverEvent {
                            HoverEvent.showText(text {
                                append(Component.text("${type.descriptionId}: ").color(NamedTextColor.GREEN))
                                appendNewline()
                                append(generateAttributeInstanceText(instance))
                            }) as HoverEvent<Any>
                        }
                    }
                }
                appendNewline()
            }
        }
    }
}