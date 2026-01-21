package cc.mewcraft.wakame.command.parser

import cc.mewcraft.wakame.craftingstation.station.CraftingStation
import cc.mewcraft.wakame.craftingstation.station.CraftingStationRegistry
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

internal class CraftingStationParser<C : Any> : ArgumentParser<C, CraftingStation>, BlockingSuggestionProvider.Strings<C> {
    companion object Factory {
        fun <C : Any> stationParser(): ParserDescriptor<C, CraftingStation> {
            return ParserDescriptor.of(CraftingStationParser(), typeTokenOf<CraftingStation>())
        }

        fun <C : Any> stationComponent(): CommandComponent.Builder<C, CraftingStation> {
            return CommandComponent.builder<C, CraftingStation?>().parser(stationParser())
        }
    }

    override fun parse(commandContext: CommandContext<C>, commandInput: CommandInput): ArgumentParseResult<CraftingStation> {
        val peekStr = commandInput.peekString()
        if (peekStr !in CraftingStationRegistry.NAMES) {
            return ArgumentParseResult.failure(StationParseException(commandContext))
        }

        val readStr = commandInput.readString()
        return ArgumentParseResult.success(CraftingStationRegistry.getStation(readStr)!!)
    }

    override fun stringSuggestions(commandContext: CommandContext<C>, input: CommandInput): Iterable<String> {
        return CraftingStationRegistry.NAMES
    }
}

class StationParseException(
    context: CommandContext<*>,
) : ParserException(
    CraftingStationParser::class.java,
    context,
    StandardCaptionKeys.EXCEPTION_INVALID_ARGUMENT
)