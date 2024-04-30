package cc.mewcraft.wakame.command.parser

import cc.mewcraft.wakame.item.schema.NekoItem
import cc.mewcraft.wakame.registry.NekoItemRegistry
import cc.mewcraft.wakame.registry.NekoItemRegistry.find
import cc.mewcraft.wakame.util.typeTokenOf
import io.leangen.geantyref.TypeToken
import org.incendo.cloud.caption.StandardCaptionKeys
import org.incendo.cloud.component.CommandComponent
import org.incendo.cloud.context.CommandContext
import org.incendo.cloud.exception.parsing.ParserException
import org.incendo.cloud.parser.ArgumentParseResult
import org.incendo.cloud.parser.ParserDescriptor
import org.incendo.cloud.parser.aggregate.AggregateParser
import org.incendo.cloud.parser.aggregate.AggregateResultMapper

/**
 * The aggregate parser for items.
 *
 * @param C the sender type
 */
class ItemParser<C : Any> : AggregateParser<C, NekoItem> {
    companion object Factory {
        fun <C : Any> itemParser(): ParserDescriptor<C, NekoItem> {
            return ParserDescriptor.of(ItemParser(), typeTokenOf())
        }

        fun <C : Any> itemComponent(): CommandComponent.Builder<C, NekoItem> {
            return CommandComponent.builder<C, NekoItem?>().parser(itemParser())
        }
    }

    override fun valueType(): TypeToken<NekoItem> {
        return typeTokenOf()
    }

    override fun components(): List<CommandComponent<C>> {
        return listOf(
            ItemNamespaceParser.itemNamespaceComponent<C>().name("namespace").build(),
            ItemPathParser.itemPathComponent<C>().name("path").build()
        )
    }

    override fun mapper(): AggregateResultMapper<C, NekoItem> {
        return AggregateResultMapper agg@{ commandContext, aggregateCommandContext ->
            val namespace = aggregateCommandContext.get<String>("namespace")
            val path = aggregateCommandContext.get<String>("path")
            val item = NekoItemRegistry.INSTANCES.find(namespace, path)
            if (item == null) {
                ArgumentParseResult.failureFuture(ItemParseException(commandContext))
            } else {
                ArgumentParseResult.successFuture(item)
            }
        }
    }
}

class ItemParseException(
    context: CommandContext<*>,
) : ParserException(
    ItemParser::class.java, context, StandardCaptionKeys.EXCEPTION_INVALID_ARGUMENT
)