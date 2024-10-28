package cc.mewcraft.wakame.command.parser

import cc.mewcraft.wakame.reforge.recycle.RecyclingStation
import cc.mewcraft.wakame.reforge.recycle.WtfRecyclingStation
import cc.mewcraft.wakame.util.typeTokenOf
import org.incendo.cloud.caption.StandardCaptionKeys
import org.incendo.cloud.component.CommandComponent
import org.incendo.cloud.context.CommandContext
import org.incendo.cloud.context.CommandInput
import org.incendo.cloud.exception.parsing.ParserException
import org.incendo.cloud.parser.*
import org.incendo.cloud.suggestion.BlockingSuggestionProvider

class RecyclingStationParser<C : Any> : ArgumentParser<C, RecyclingStation>, BlockingSuggestionProvider.Strings<C> {
    companion object Factory {
        fun <C : Any> recyclingStationParser(): ParserDescriptor<C, RecyclingStation> {
            return ParserDescriptor.of(RecyclingStationParser(), typeTokenOf<RecyclingStation>())
        }

        fun <C : Any> recyclingStationComponent(): CommandComponent.Builder<C, RecyclingStation> {
            return CommandComponent.builder<C, RecyclingStation?>().parser(RecyclingStationParser())
        }
    }

    override fun parse(commandContext: CommandContext<C>, commandInput: CommandInput): ArgumentParseResult<RecyclingStation> {
        // val peekStr = commandInput.peekString()
        // if (peekStr !in RecyclingStationRegistry.NAMES) {
        //     return ArgumentParseResult.failure(RecyclingStationParseException(commandContext))
        // }
        //
        // val readStr = commandInput.readString()
        // return ArgumentParseResult.success(RecyclingStationRegistry[readStr]!!)
        // TODO recycling
        return ArgumentParseResult.success(WtfRecyclingStation)
    }

    override fun stringSuggestions(commandContext: CommandContext<C>, input: CommandInput): Iterable<String> {
        // return RecyclingStationRegistry.NAMES
        // TODO recycling
        return listOf()
    }
}

class RecyclingStationParseException(
    context: CommandContext<*>,
) : ParserException(
    ModdingTableParser::class.java,
    context,
    StandardCaptionKeys.EXCEPTION_INVALID_ARGUMENT
)