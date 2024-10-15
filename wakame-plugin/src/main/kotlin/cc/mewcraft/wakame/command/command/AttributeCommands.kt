package cc.mewcraft.wakame.command.command

import cc.mewcraft.wakame.attribute.*
import cc.mewcraft.wakame.command.CommandConstants
import cc.mewcraft.wakame.command.CommandPermissions
import cc.mewcraft.wakame.command.buildAndAdd
import cc.mewcraft.wakame.command.parser.AttributeModifierOperationParser
import cc.mewcraft.wakame.command.parser.AttributeParser
import me.lucko.helper.text3.mini
import net.kyori.adventure.extra.kotlin.join
import net.kyori.adventure.extra.kotlin.text
import net.kyori.adventure.extra.kotlin.translatable
import net.kyori.adventure.inventory.Book
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.NamespacedKey
import org.bukkit.command.CommandSender
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.incendo.cloud.Command
import org.incendo.cloud.CommandFactory
import org.incendo.cloud.CommandManager
import org.incendo.cloud.bukkit.data.SingleEntitySelector
import org.incendo.cloud.bukkit.parser.NamespacedKeyParser
import org.incendo.cloud.bukkit.parser.selector.SingleEntitySelectorParser
import org.incendo.cloud.description.Description
import org.incendo.cloud.kotlin.coroutines.extension.suspendingHandler
import org.incendo.cloud.kotlin.extension.commandBuilder
import org.incendo.cloud.kotlin.extension.getOrNull
import org.incendo.cloud.parser.standard.DoubleParser
import java.text.DecimalFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.collections.component1
import kotlin.collections.component2

private const val ATTRIBUTE_COUNT_PER_PAGE = 14

object AttributeCommands : CommandFactory<CommandSender> {
    private const val ATTRIBUTE_LITERAL = "attribute"

    private fun sendNoAttributeMessage(sender: CommandSender, source: Entity) {
        sender.sendMessage(text {
            color(NamedTextColor.RED)
            content("The entity has no custom attributes.")
            hoverEvent(HoverEvent.showText(Component.text("Click to copy the UUID of this entity")))
            clickEvent(ClickEvent.copyToClipboard(source.uniqueId.toString()))
        })
    }

    private fun sendNoAttributeInstanceMessage(sender: CommandSender, source: Entity, attribute: Attribute) {
        sender.sendMessage(text {
            append("The entity does not have the attribute ".mini)
            append(Component.text(attribute.descriptionId).color(NamedTextColor.YELLOW))
            append(".".mini)
            hoverEvent(HoverEvent.showText(Component.text("Click to copy the UUID of this entity")))
            clickEvent(ClickEvent.copyToClipboard(source.uniqueId.toString()))
        })
    }

    override fun createCommands(commandManager: CommandManager<CommandSender>): List<Command<out CommandSender>> {
        return buildList {
            // /<root> attribute base get [entity] [attribute]
            commandManager.commandBuilder(
                name = CommandConstants.ROOT_COMMAND,
                description = Description.of("Get the base value of an attribute")
            ) {
                permission(CommandPermissions.ATTRIBUTE)
                literal(ATTRIBUTE_LITERAL)
                literal("base")
                literal("get")
                required("source", SingleEntitySelectorParser.singleEntitySelectorParser())
                required("attribute", AttributeParser.attributeParser())
                suspendingHandler { context ->
                    val sender = context.sender()
                    val source = context.get<SingleEntitySelector>("source").single()
                    val attribute = context.get<Attribute>("attribute")

                    val attributeMap = getAttributeMap(source) {
                        sendNoAttributeMessage(sender, source)
                        return@suspendingHandler
                    }.getSnapshot()

                    val baseValue = attributeMap.getBaseValue(attribute)

                    sender.sendMessage(text {
                        append("The base value of ".mini)
                        append(Component.text(attribute.descriptionId).color(NamedTextColor.YELLOW))
                        append(" is ".mini)
                        append(Component.text(DecimalFormat("#.##").format(baseValue)).color(NamedTextColor.GREEN))
                    })
                }
            }.buildAndAdd(this)

            // /<root> attribute base set [entity] [attribute] [value]
            commandManager.commandBuilder(
                name = CommandConstants.ROOT_COMMAND,
                description = Description.of("Set the base value of an attribute")
            ) {
                permission(CommandPermissions.ATTRIBUTE)
                literal(ATTRIBUTE_LITERAL)
                literal("base")
                literal("set")
                required("source", SingleEntitySelectorParser.singleEntitySelectorParser())
                required("attribute", AttributeParser.attributeParser())
                required("value", DoubleParser.doubleParser())
                suspendingHandler { context ->
                    val sender = context.sender()
                    val source = context.get<SingleEntitySelector>("source").single()
                    val attribute = context.get<Attribute>("attribute")
                    val value = context.get<Double>("value")

                    val attributeMap = getAttributeMap(source) {
                        sendNoAttributeMessage(sender, source)
                        return@suspendingHandler
                    }

                    val instance = attributeMap.getInstance(attribute)

                    if (instance == null) {
                        sendNoAttributeInstanceMessage(sender, source, attribute)
                        return@suspendingHandler
                    }

                    instance.setBaseValue(value)
                    sender.sendMessage(text {
                        append("The base value of ".mini)
                        append(Component.text(attribute.descriptionId).color(NamedTextColor.YELLOW))
                        append(" has been set to ".mini)
                        append(Component.text(DecimalFormat("#.##").format(value)).color(NamedTextColor.GREEN))
                    })
                }
            }.buildAndAdd(this)

            // /<root> attribute modifier add [entity] [attribute] [id] [operation] [source]
            commandManager.commandBuilder(
                name = CommandConstants.ROOT_COMMAND,
                description = Description.of("Add a modifier to an attribute")
            ) {
                permission(CommandPermissions.ATTRIBUTE)
                literal(ATTRIBUTE_LITERAL)
                literal("modifier")
                literal("add")
                required("source", SingleEntitySelectorParser.singleEntitySelectorParser())
                required("attribute", AttributeParser.attributeParser())
                required("id", NamespacedKeyParser.namespacedKeyParser())
                required("operation", AttributeModifierOperationParser.attributeModifierOperationParser())
                required("amount", DoubleParser.doubleParser())
                suspendingHandler { context ->
                    val sender = context.sender()
                    val source = context.get<SingleEntitySelector>("source").single()
                    val attribute = context.get<Attribute>("attribute")
                    val id = context.get<NamespacedKey>("id")
                    val operation = context.get<AttributeModifier.Operation>("operation")
                    val amount = context.get<Double>("amount")
                    val modifier = AttributeModifier(id, amount, operation)

                    val attributeMap = getAttributeMap(source) {
                        sendNoAttributeMessage(sender, source)
                        return@suspendingHandler
                    }

                    val instance = attributeMap.getInstance(attribute)

                    if (instance == null) {
                        sendNoAttributeInstanceMessage(sender, source, attribute)
                        return@suspendingHandler
                    }

                    instance.addModifier(modifier)
                    sender.sendMessage(text {
                        append("A modifier has been added to ".mini)
                        append(Component.text(attribute.descriptionId).color(NamedTextColor.YELLOW))
                        append(" with ".mini)
                        append(Component.text(modifier.id.asString()).color(NamedTextColor.GREEN))
                        append(" ".mini)
                        append(Component.text(modifier.operation.name).color(NamedTextColor.GREEN))
                        append(" ".mini)
                        append(Component.text(DecimalFormat("#.##").format(modifier.amount)).color(NamedTextColor.GREEN))
                    })
                }
            }.buildAndAdd(this)

            // /<root> attribute modifier remove [entity] [attribute] [id]
            commandManager.commandBuilder(
                name = CommandConstants.ROOT_COMMAND,
                description = Description.of("Remove a modifier from an attribute")
            ) {
                permission(CommandPermissions.ATTRIBUTE)
                literal(ATTRIBUTE_LITERAL)
                literal("modifier")
                literal("remove")
                required("source", SingleEntitySelectorParser.singleEntitySelectorParser())
                required("attribute", AttributeParser.attributeParser())
                required("id", NamespacedKeyParser.namespacedKeyParser())
                suspendingHandler { context ->
                    val sender = context.sender()
                    val source = context.get<SingleEntitySelector>("source").single()
                    val attribute = context.get<Attribute>("attribute")
                    val id = context.get<NamespacedKey>("id")

                    val attributeMap = getAttributeMap(source) {
                        sendNoAttributeMessage(sender, source)
                        return@suspendingHandler
                    }

                    val instance = attributeMap.getInstance(attribute)

                    if (instance == null) {
                        sendNoAttributeInstanceMessage(sender, source, attribute)
                        return@suspendingHandler
                    }

                    if (instance.getModifiers().none { it.id == id }) {
                        sender.sendMessage(text {
                            append("The attribute ".mini)
                            append(Component.text(attribute.descriptionId).color(NamedTextColor.YELLOW))
                            append(" does not have a modifier with the id ".mini)
                            append(Component.text(id.asString()).color(NamedTextColor.RED))
                            append(".".mini)
                        })
                        return@suspendingHandler
                    }

                    instance.removeModifier(id)
                    sender.sendMessage(text {
                        append("The modifier with the id ".mini)
                        append(Component.text(id.asString()).color(NamedTextColor.GREEN))
                        append(" has been removed from ".mini)
                        append(Component.text(attribute.descriptionId).color(NamedTextColor.YELLOW))
                    })
                }
            }.buildAndAdd(this)

            // /<root> attribute report [entity] [--print]
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

                    val attributeMap = getAttributeMap(source) {
                        sendNoAttributeMessage(sender, source)
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

    private inline fun getAttributeMap(entity: Entity, default: () -> AttributeMap): AttributeMap {
        return when (entity) {
            is Player -> PlayerAttributeAccessor.getAttributeMap(entity)
            is LivingEntity -> EntityAttributeAccessor.getAttributeMap(entity)
            else -> default()
        }
    }
}

object AttributeReport {
    private val DECIMAL_FORMAT = DecimalFormat("#.##")
    private val DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    fun generateBook(
        source: Entity,
        attributes: AttributeMap,
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
        attributes: AttributeMap,
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
        instance: AttributeInstance,
    ): Component {
        return text {
            content(instance.attribute.descriptionId + ": ")
            color(NamedTextColor.GREEN)
            append {
                text {
                    content(DECIMAL_FORMAT.format(instance.getValue()))
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
        attributeList: List<Map.Entry<Attribute, AttributeInstance>>,
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