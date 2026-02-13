package cc.mewcraft.wakame.command.parser

import cc.mewcraft.wakame.item.KoishItem
import cc.mewcraft.wakame.registry.BuiltInRegistries
import cc.mewcraft.wakame.util.KoishKey
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
 * The parser for [KoishItem].
 *
 * @param C the sender type
 */
class ItemParser<C : Any> : ArgumentParser<C, KoishItem>, BlockingSuggestionProvider.Strings<C> {
    companion object Factory {
        fun <C : Any> itemParser(): ParserDescriptor<C, KoishItem> {
            return ParserDescriptor.of(ItemParser(), typeTokenOf())
        }

        fun <C : Any> itemComponent(): CommandComponent.Builder<C, KoishItem> {
            return CommandComponent.builder<C, KoishItem>().parser(itemParser())
        }
    }

    override fun parse(commandContext: CommandContext<C>, commandInput: CommandInput): ArgumentParseResult<KoishItem> {
        val peekString = commandInput.peekString()
        val itemType = BuiltInRegistries.ITEM[peekString] ?: return ArgumentParseResult.failure(ItemParseException(commandContext))
        commandInput.readString() // consume token
        return ArgumentParseResult.success(itemType)
    }

    override fun stringSuggestions(commandContext: CommandContext<C>, input: CommandInput): Iterable<String> {
        return BuiltInRegistries.ITEM.ids.map(KoishKey::asString)
    }
}

class ItemParseException(
    context: CommandContext<*>,
) : ParserException(
    ItemParser::class.java,
    context,
    StandardCaptionKeys.EXCEPTION_INVALID_ARGUMENT
)