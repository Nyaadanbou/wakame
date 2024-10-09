package cc.mewcraft.wakame.command.command

import cc.mewcraft.wakame.attribute.*
import cc.mewcraft.wakame.command.CommandConstants
import cc.mewcraft.wakame.command.CommandPermissions
import cc.mewcraft.wakame.command.buildAndAdd
import net.kyori.adventure.extra.kotlin.join
import net.kyori.adventure.extra.kotlin.text
import net.kyori.adventure.extra.kotlin.translatable
import net.kyori.adventure.inventory.Book
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.incendo.cloud.Command
import org.incendo.cloud.CommandFactory
import org.incendo.cloud.CommandManager
import org.incendo.cloud.bukkit.data.SingleEntitySelector
import org.incendo.cloud.bukkit.parser.selector.SingleEntitySelectorParser
import org.incendo.cloud.description.Description
import org.incendo.cloud.kotlin.coroutines.extension.suspendingHandler
import org.incendo.cloud.kotlin.extension.commandBuilder
import org.incendo.cloud.kotlin.extension.getOrNull
import java.text.DecimalFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.collections.component1
import kotlin.collections.component2

private const val ATTRIBUTE_COUNT_PER_PAGE = 14

object AttributeCommands : CommandFactory<CommandSender> {
    private const val ATTRIBUTE_LITERAL = "attribute"

    override fun createCommands(commandManager: CommandManager<CommandSender>): List<Command<out CommandSender>> {
        return buildList {
            // /<root> attribute [entity] [--print]
            commandManager.commandBuilder(
                name = CommandConstants.ROOT_COMMAND,
                description = Description.of("Show the entity(s) attributes")
            ) {
                permission(CommandPermissions.ATTRIBUTE)
                literal(ATTRIBUTE_LITERAL)
                literal("report")
                optional("source", SingleEntitySelectorParser.singleEntitySelectorParser())
                // optional("recipient", MultiplePlayerSelectorParser.multiplePlayerSelectorParser())
                flag("print", description = Description.of("Give a book containing the result to the sender's inventory"))
                suspendingHandler { context ->
                    val sender = context.sender()
                    val source = context.getOrNull<SingleEntitySelector>("source")?.single() ?: sender as? Entity
                    if (source == null) {
                        sender.sendMessage(text {
                            content("The source entity is not an entity.")
                            color(NamedTextColor.RED)
                        })
                        return@suspendingHandler
                    }

                    val printing = context.flags().contains("print")
                    // val recipients = context.getOrNull<MultiplePlayerSelector>("recipient")?.values()
                    //     ?: (if (sender is Player) listOf(sender) else emptyList())

                    val attributeMap = when (source) {
                        is Player -> PlayerAttributeAccessor.getAttributeMap(source).getSnapshot()
                        is LivingEntity -> EntityAttributeAccessor.getAttributeMap(source).getSnapshot()
                        else -> null
                    }
                    if (attributeMap == null) {
                        sender.sendMessage(text {
                            color(NamedTextColor.RED)
                            content("The entity has no custom attributes.")
                            hoverEvent(HoverEvent.showText(Component.text("Click to copy the UUID of this entity")))
                            clickEvent(ClickEvent.copyToClipboard(source.uniqueId.toString()))
                        })
                        return@suspendingHandler
                    }

                    sender.sendMessage(AttributeReport.generateText(source, attributeMap))

                    if (printing) {
                        sender.openBook(AttributeReport.generateBook(source, attributeMap))
                    }
                }
            }.buildAndAdd(this)
        }
    }
}

object AttributeReport {
    private val DECIMAL_FORMAT = DecimalFormat("#.##")
    private val DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    fun generateBook(
        source: Entity,
        attributes: AttributeMapSnapshot,
    ): Book {
        val sortedAttributes = attributes.sortedBy { (type, _) -> type.descriptionId }
        val pages = generateBookPages(sortedAttributes)
        val book = Book.builder()
            .title(text {
                val sourceName =
                    if (source is Player) {
                        source.name()
                    } else {
                        translatable { key(source.type) }
                    }
                content("属性报告之 ")
                append(sourceName)
            })
            .author(Component.text("Nyaadanbou"))
            .pages(pages)
            .build()

        return book
    }

    fun generateText(
        source: Entity,
        attributes: AttributeMapSnapshot,
    ): Component {
        val sorted = attributes.sortedBy { (type, _) -> type.descriptionId }
        val result = text {
            content("Hover to check the attribute report of ")
            append(source.name())
            hoverEvent(
                HoverEvent.showText(
                    sorted
                        .map { (_, instance) -> generateSingleText(instance) }
                        .join(JoinConfiguration.newlines())
                )
            )
        }
        return result
    }

    fun generateSingleText(
        instance: AttributeInstanceSnapshot,
    ): Component {
        return text {
            content(instance.attribute.descriptionId + ": ")
            color(NamedTextColor.GREEN)
            append {
                text {
                    content("${DECIMAL_FORMAT.format(instance.getValue())}")
                    color(NamedTextColor.YELLOW)
                }
            }
            appendNewline()
            append {
                text {
                    content("  base: ${DECIMAL_FORMAT.format(instance.getBaseValue())}")
                    color(NamedTextColor.GRAY)
                }
            }
            for (modifier in instance.getModifiers()) {
                appendNewline()
                append {
                    text {
                        val op = modifier.operation.name.lowercase()
                        val value = DECIMAL_FORMAT.format(modifier.amount)
                        val source = modifier.id
                        content("  $op: $value $source")
                        color(NamedTextColor.GRAY)
                    }
                }
            }
        }
    }

    private fun currentTime(): String {
        return LocalDateTime.now().format(DATE_TIME_FORMATTER)
    }

    private fun generateBookPages(
        attributeList: List<Map.Entry<Attribute, AttributeInstanceSnapshot>>
    ): List<Component> {
        val result = attributeList
            .chunked(ATTRIBUTE_COUNT_PER_PAGE)
            .map { chunk ->
                // 生成当前页面的属性报告列表
                // 每个元素是单个属性的报告文本
                val texts = chunk.map { (type, instance) ->
                    text {
                        append {
                            text {
                                if (type.descriptionId.length > 20) {
                                    content(type.descriptionId.substring(0, 17) + "...")
                                    color(NamedTextColor.DARK_GREEN)
                                } else {
                                    content(type.descriptionId)
                                    color(NamedTextColor.DARK_GREEN)
                                }
                                hoverEvent(HoverEvent.showText(text {
                                    append(Component.text("${type.descriptionId}: ").color(NamedTextColor.GREEN))
                                    appendNewline()
                                    append(generateSingleText(instance))
                                }))
                            }
                        }
                        appendNewline()
                    }
                }
                // 然后把所有属性报告拼拼接成一整个文本
                val joined = texts.join(JoinConfiguration.newlines())

                joined
            }

        return buildList {
            this += text {
                content("报告生成于")
                appendNewline()
                appendNewline()
                append(text { content(currentTime()) })
            }
            this += result
        } // 列表中的每个元素代表书的一页
    }
}