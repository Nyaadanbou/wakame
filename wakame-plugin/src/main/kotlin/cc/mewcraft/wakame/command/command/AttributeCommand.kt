package cc.mewcraft.wakame.command.command

import cc.mewcraft.wakame.attribute.*
import cc.mewcraft.wakame.command.CommandPermissions
import cc.mewcraft.wakame.command.KoishCommandFactory
import cc.mewcraft.wakame.command.koishHandler
import cc.mewcraft.wakame.command.parser.AttributeModifierOperationParser
import cc.mewcraft.wakame.command.parser.AttributeParser
import cc.mewcraft.wakame.entity.attribute.*
import cc.mewcraft.wakame.util.coroutine.minecraft
import cc.mewcraft.wakame.util.text.mini
import kotlinx.coroutines.Dispatchers
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
import org.bukkit.entity.Player
import org.incendo.cloud.bukkit.data.SingleEntitySelector
import org.incendo.cloud.bukkit.parser.NamespacedKeyParser
import org.incendo.cloud.bukkit.parser.selector.SingleEntitySelectorParser
import org.incendo.cloud.context.CommandContext
import org.incendo.cloud.description.Description
import org.incendo.cloud.kotlin.extension.getOrNull
import org.incendo.cloud.paper.util.sender.Source
import org.incendo.cloud.parser.standard.DoubleParser
import java.text.DecimalFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private const val ATTRIBUTE_COUNT_PER_PAGE = 14

internal object AttributeCommand : KoishCommandFactory<Source> {

    override fun KoishCommandFactory.Builder<Source>.createCommands() {
        val commonBuilder = build {
            permission(CommandPermissions.ATTRIBUTE)
            literal("attribute")
        }

        // <root> attribute base get <entity> <attribute>
        // Get the base value of an attribute
        buildAndAdd(commonBuilder) {
            literal("base")
            literal("get")
            required("source", SingleEntitySelectorParser.singleEntitySelectorParser())
            required("attribute", AttributeParser.attributeParser())
            koishHandler(context = Dispatchers.minecraft, handler = ::handleGetBaseValue)
        }

        // <root> attribute base set <entity> <attribute> <value>
        // Set the base value of an attribute
        buildAndAdd(commonBuilder) {
            literal("base")
            literal("set")
            required("source", SingleEntitySelectorParser.singleEntitySelectorParser())
            required("attribute", AttributeParser.attributeParser())
            required("value", DoubleParser.doubleParser())
            koishHandler(context = Dispatchers.minecraft, handler = ::handleSetBaseValue)
        }

        // <root> attribute modifier add <entity> <attribute> <id> <operation> <source>
        // Add a modifier to an attribute
        buildAndAdd(commonBuilder) {
            literal("modifier")
            literal("add")
            required("source", SingleEntitySelectorParser.singleEntitySelectorParser())
            required("attribute", AttributeParser.attributeParser())
            required("id", NamespacedKeyParser.namespacedKeyParser())
            required("operation", AttributeModifierOperationParser.attributeModifierOperationParser())
            required("amount", DoubleParser.doubleParser())
            koishHandler(context = Dispatchers.minecraft, handler = ::handleAddModifier)
        }

        // <root> attribute modifier remove <entity> <attribute> <id>
        // Remove a modifier from an attribute
        buildAndAdd(commonBuilder) {
            literal("modifier")
            literal("remove")
            required("source", SingleEntitySelectorParser.singleEntitySelectorParser())
            required("attribute", AttributeParser.attributeParser())
            required("id", NamespacedKeyParser.namespacedKeyParser())
            koishHandler(context = Dispatchers.minecraft, handler = ::handleRemoveModifier)
        }

        // <root> attribute report [entity] [--print]
        // Show the attributes of specific entity(s)
        buildAndAdd(commonBuilder) {
            literal("report")
            optional("source", SingleEntitySelectorParser.singleEntitySelectorParser())
            flag("print", description = Description.of("Give a book containing the result to the sender's inventory"))
            koishHandler(context = Dispatchers.minecraft, handler = ::handleReport)
        }
    }

    private fun handleGetBaseValue(context: CommandContext<Source>) {
        val sender = context.sender().source()
        val source = context.get<SingleEntitySelector>("source").single()
        val attribute = context.get<Attribute>("attribute")

        val attributeMap = getAttributeMap(source)?.getSnapshot() ?: run {
            sendNoAttributeMessage(sender, source)
            return
        }

        val baseValue = attributeMap.getBaseValue(attribute)

        sender.sendMessage(text {
            append("The base value of ".mini)
            append(Component.text(attribute.id).color(NamedTextColor.YELLOW))
            append(" is ".mini)
            append(Component.text(DecimalFormat("#.##").format(baseValue)).color(NamedTextColor.GREEN))
        })
    }

    private fun handleSetBaseValue(context: CommandContext<Source>) {
        val sender = context.sender().source()
        val source = context.get<SingleEntitySelector>("source").single()
        val attribute = context.get<Attribute>("attribute")
        val value = context.get<Double>("value")

        val attributeMap = getAttributeMap(source) ?: run {
            sendNoAttributeMessage(sender, source)
            return
        }

        val instance = attributeMap.getInstance(attribute)

        if (instance == null) {
            sendNoAttributeInstanceMessage(sender, source, attribute)
            return
        }

        instance.setBaseValue(value)
        sender.sendMessage(text {
            append("The base value of ".mini)
            append(Component.text(attribute.id).color(NamedTextColor.YELLOW))
            append(" has been set to ".mini)
            append(Component.text(DecimalFormat("#.##").format(value)).color(NamedTextColor.GREEN))
        })
    }

    private fun handleAddModifier(context: CommandContext<Source>) {
        val sender = context.sender().source()
        val source = context.get<SingleEntitySelector>("source").single()
        val attribute = context.get<Attribute>("attribute")
        val id = context.get<NamespacedKey>("id")
        val operation = context.get<AttributeModifier.Operation>("operation")
        val amount = context.get<Double>("amount")
        val modifier = AttributeModifier(id, amount, operation)

        val attributeMap = getAttributeMap(source) ?: run {
            sendNoAttributeMessage(sender, source)
            return
        }

        val instance = attributeMap.getInstance(attribute)

        if (instance == null) {
            sendNoAttributeInstanceMessage(sender, source, attribute)
            return
        }

        if (instance.hasModifier(modifier)) {
            sender.sendMessage(text {
                append("The attribute ".mini)
                append(Component.text(attribute.id).color(NamedTextColor.YELLOW))
                append(" already has a modifier with the id ".mini)
                append(Component.text(id.asString()).color(NamedTextColor.RED))
                append(".".mini)
            })
            return
        }

        instance.addTransientModifier(modifier)
        sender.sendMessage(text {
            append("A modifier has been added to ".mini)
            append(Component.text(attribute.id).color(NamedTextColor.YELLOW))
            append(" with ".mini)
            append(Component.text(modifier.id.asString()).color(NamedTextColor.GREEN))
            append(" ".mini)
            append(Component.text(modifier.operation.name).color(NamedTextColor.GREEN))
            append(" ".mini)
            append(Component.text(DecimalFormat("#.##").format(modifier.amount)).color(NamedTextColor.GREEN))
        })
    }

    private fun handleRemoveModifier(context: CommandContext<Source>) {
        val sender = context.sender().source()
        val source = context.get<SingleEntitySelector>("source").single()
        val attribute = context.get<Attribute>("attribute")
        val id = context.get<NamespacedKey>("id")

        val attributeMap = getAttributeMap(source) ?: run {
            sendNoAttributeMessage(sender, source)
            return
        }

        val instance = attributeMap.getInstance(attribute)

        if (instance == null) {
            sendNoAttributeInstanceMessage(sender, source, attribute)
            return
        }

        if (instance.getModifiers().none { it.id == id }) {
            sender.sendMessage(text {
                append("The attribute ".mini)
                append(Component.text(attribute.id).color(NamedTextColor.YELLOW))
                append(" does not have a modifier with the id ".mini)
                append(Component.text(id.asString()).color(NamedTextColor.RED))
                append(".".mini)
            })
            return
        }

        instance.removeModifier(id)
        sender.sendMessage(text {
            append("The modifier with the id ".mini)
            append(Component.text(id.asString()).color(NamedTextColor.GREEN))
            append(" has been removed from ".mini)
            append(Component.text(attribute.id).color(NamedTextColor.YELLOW))
        })
    }

    private fun handleReport(context: CommandContext<Source>) {
        val sender = context.sender().source()
        val source = context.getOrNull<SingleEntitySelector>("source")?.single() ?: sender as? Entity
        if (source == null) {
            sender.sendMessage(text {
                content("The source entity is not an entity.")
                color(NamedTextColor.RED)
            })
            return
        }

        val shouldPrint = context.flags().contains("print")
        val attributeMap = getAttributeMap(source) ?: run {
            sendNoAttributeMessage(sender, source)
            return
        }

        sender.sendMessage(AttributeReport.generateText(source, attributeMap))

        if (shouldPrint) {
            sender.openBook(AttributeReport.generateBook(source, attributeMap))
        }
    }

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
            append(Component.text(attribute.id).color(NamedTextColor.YELLOW))
            append(".".mini)
            hoverEvent(HoverEvent.showText(Component.text("Click to copy the UUID of this entity")))
            clickEvent(ClickEvent.copyToClipboard(source.uniqueId.toString()))
        })
    }

    private fun getAttributeMap(entity: Entity): AttributeMap? {
        return AttributeMapAccess.INSTANCE.get(entity).getOrNull()
    }

}

/**
 * 封装了用于生成一个 [Entity] 的属性信息的逻辑.
 */
private object AttributeReport {
    private val DECIMAL_FORMAT = DecimalFormat("#.##")
    private val DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    fun generateBook(source: Entity, attributes: AttributeMap): Book {
        val sortedAttributes = attributes.sortedBy { (type, _) -> type.id }
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

    fun generateText(source: Entity, attributes: AttributeMap): Component {
        val sorted = attributes.sortedBy { (type, _) -> type.id }
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

    private fun generateSingleText(instance: AttributeInstance): Component {
        return text {
            content(instance.attribute.id + ": ")
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

    private fun generateBookPages(attributeList: List<Map.Entry<Attribute, AttributeInstance>>): List<Component> {
        val result = attributeList
            .chunked(ATTRIBUTE_COUNT_PER_PAGE)
            .map { chunk ->
                // 生成当前页面的属性报告列表
                // 每个元素是单个属性的报告文本
                val texts = chunk.map { (type, instance) ->
                    text {
                        append {
                            text {
                                if (type.id.length > 20) {
                                    content(type.id.substring(0, 17) + "...")
                                    color(NamedTextColor.DARK_GREEN)
                                } else {
                                    content(type.id)
                                    color(NamedTextColor.DARK_GREEN)
                                }
                                hoverEvent(HoverEvent.showText(text {
                                    append(Component.text("${type.id}: ").color(NamedTextColor.GREEN))
                                    appendNewline()
                                    append(generateSingleText(instance))
                                }))
                            }
                        }
                        appendNewline()
                    }
                }

                // 然后把所有属性报告拼拼接成一整个文本
                texts.join(JoinConfiguration.newlines())
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