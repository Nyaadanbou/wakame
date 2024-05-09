package cc.mewcraft.wakame.command.parser

import cc.mewcraft.wakame.registry.ItemRegistry
import cc.mewcraft.wakame.util.typeTokenOf
import org.incendo.cloud.caption.StandardCaptionKeys
import org.incendo.cloud.component.CommandComponent
import org.incendo.cloud.context.CommandContext
import org.incendo.cloud.context.CommandInput
import org.incendo.cloud.exception.parsing.ParserException
import org.incendo.cloud.kotlin.extension.getOrNull
import org.incendo.cloud.parser.ArgumentParseResult
import org.incendo.cloud.parser.ArgumentParser
import org.incendo.cloud.parser.ParserDescriptor
import org.incendo.cloud.suggestion.BlockingSuggestionProvider

/**
 * The parser for item paths.
 *
 * It must be used together with [ItemNamespaceParser].
 *
 * @param C the sender type
 */
class ItemPathParser<C : Any> : ArgumentParser<C, String>, BlockingSuggestionProvider.Strings<C> {
    companion object Factory {
        fun <C : Any> itemPathParser(): ParserDescriptor<C, String> {
            return ParserDescriptor.of(ItemPathParser(), typeTokenOf())
        }

        fun <C : Any> itemPathComponent(): CommandComponent.Builder<C, String> {
            return CommandComponent.builder<C, String>().parser(itemPathParser())
        }
    }

    override fun parse(commandContext: CommandContext<C>, commandInput: CommandInput): ArgumentParseResult<String> {
        val namespaceHint = commandContext.getOrNull<String>(ItemNamespaceParser.NAMESPACE_HINT)
        val paths = ItemRegistry.PATHS_BY_NAMESPACE[namespaceHint]
                    ?: return ArgumentParseResult.failure(ItemPathParseException(commandContext))

        val peekString = commandInput.peekString()
        if (peekString !in paths) {
            return ArgumentParseResult.failure(ItemPathParseException(commandContext))
        }

        val readString = commandInput.readString()
        return ArgumentParseResult.success(readString)
    }

    override fun stringSuggestions(commandContext: CommandContext<C>, input: CommandInput): Iterable<String> {
        val namespaceHint = commandContext.getOrNull<String>(ItemNamespaceParser.NAMESPACE_HINT)
        return ItemRegistry.PATHS_BY_NAMESPACE[namespaceHint] ?: emptyList()
    }
}

class ItemPathParseException(
    context: CommandContext<*>,
) : ParserException(
    ItemPathParser::class.java,
    context,
    StandardCaptionKeys.EXCEPTION_INVALID_ARGUMENT
)