package cc.mewcraft.wakame.command.parser

import cc.mewcraft.wakame.craftingstation.Station
import cc.mewcraft.wakame.craftingstation.StationRegistry
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

internal class StationParser<C : Any> : ArgumentParser<C, Station>, BlockingSuggestionProvider.Strings<C> {
    companion object Factory {
        fun <C : Any> stationParser(): ParserDescriptor<C, Station> {
            return ParserDescriptor.of(StationParser(), typeTokenOf<Station>())
        }

        fun <C : Any> stationComponent(): CommandComponent.Builder<C, Station> {
            return CommandComponent.builder<C, Station?>().parser(stationParser())
        }
    }

    override fun parse(commandContext: CommandContext<C>, commandInput: CommandInput): ArgumentParseResult<Station> {
        val peekStr = commandInput.peekString()
        if (peekStr !in StationRegistry.NAMES) {
            return ArgumentParseResult.failure(StationParseException(commandContext))
        }

        val readStr = commandInput.readString()
        return ArgumentParseResult.success(StationRegistry.find(readStr)!!)
    }

    override fun stringSuggestions(commandContext: CommandContext<C>, input: CommandInput): Iterable<String> {
        return StationRegistry.NAMES
    }
}

class StationParseException(
    context: CommandContext<*>,
) : ParserException(
    StationParser::class.java,
    context,
    StandardCaptionKeys.EXCEPTION_INVALID_ARGUMENT
)