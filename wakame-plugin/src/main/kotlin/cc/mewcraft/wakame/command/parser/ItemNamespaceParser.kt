package cc.mewcraft.wakame.command.parser

import cc.mewcraft.wakame.registry.NekoItemRegistry
import cc.mewcraft.wakame.util.typeTokenOf
import org.incendo.cloud.caption.StandardCaptionKeys
import org.incendo.cloud.component.CommandComponent
import org.incendo.cloud.context.CommandContext
import org.incendo.cloud.context.CommandInput
import org.incendo.cloud.exception.parsing.ParserException
import org.incendo.cloud.parser.ArgumentParseResult
import org.incendo.cloud.parser.ArgumentParser
import org.incendo.cloud.parser.ParserDescriptor
import org.incendo.cloud.suggestion.BlockingSuggestionProvider

/**
 * The parser for item namespaces.
 *
 * It must be used together with [ItemPathParser].
 *
 * @param C the sender type
 */
class ItemNamespaceParser<C : Any> : ArgumentParser<C, String>, BlockingSuggestionProvider.Strings<C> {
    companion object Factory {
        const val NAMESPACE_HINT = "namespace_hint"

        fun <C : Any> itemNamespaceParser(): ParserDescriptor<C, String> {
            return ParserDescriptor.of(ItemNamespaceParser(), typeTokenOf<String>())
        }

        fun <C : Any> itemNamespaceComponent(): CommandComponent.Builder<C, String> {
            return CommandComponent.builder<C, String?>().parser(itemNamespaceParser())
        }
    }

    override fun parse(commandContext: CommandContext<C>, commandInput: CommandInput): ArgumentParseResult<String> {
        val peekString = commandInput.peekString()
        if (peekString !in NekoItemRegistry.NAMESPACES) {
            return ArgumentParseResult.failure(ItemNamespaceParseException(commandContext))
        }

        val readString = commandInput.readString()
        commandContext.store(NAMESPACE_HINT, readString) // store the namespace hint for the subsequent path parsing
        return ArgumentParseResult.success(readString)
    }

    override fun stringSuggestions(commandContext: CommandContext<C>, input: CommandInput): Iterable<String> {
        return NekoItemRegistry.NAMESPACES
    }
}

class ItemNamespaceParseException(
    context: CommandContext<*>,
) : ParserException(
    ItemNamespaceParser::class.java,
    context,
    StandardCaptionKeys.EXCEPTION_INVALID_ARGUMENT
)