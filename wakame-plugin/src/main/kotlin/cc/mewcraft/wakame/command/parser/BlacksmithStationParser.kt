package cc.mewcraft.wakame.command.parser

import cc.mewcraft.wakame.reforge.blacksmith.BlacksmithStation
import cc.mewcraft.wakame.reforge.blacksmith.BlacksmithStationRegistry
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

class BlacksmithStationParser<C : Any> : ArgumentParser<C, BlacksmithStation>, BlockingSuggestionProvider.Strings<C> {
    companion object Factory {
        fun <C : Any> blacksmithStationParser(): ParserDescriptor<C, BlacksmithStation> {
            return ParserDescriptor.of(BlacksmithStationParser(), typeTokenOf<BlacksmithStation>())
        }

        fun <C : Any> blacksmithStationComponent(): CommandComponent.Builder<C, BlacksmithStation> {
            return CommandComponent.builder<C, BlacksmithStation?>().parser(BlacksmithStationParser())
        }
    }

    override fun parse(commandContext: CommandContext<C>, commandInput: CommandInput): ArgumentParseResult<BlacksmithStation> {
        val peekStr = commandInput.peekString()
        if (peekStr !in BlacksmithStationRegistry.NAMES) {
            return ArgumentParseResult.failure(BlacksmithStationParseException(commandContext))
        }

        val readStr = commandInput.readString()
        return ArgumentParseResult.success(BlacksmithStationRegistry.getStation(readStr)!!)
    }

    override fun stringSuggestions(commandContext: CommandContext<C>, input: CommandInput): Iterable<String> {
        return BlacksmithStationRegistry.NAMES
    }
}

class BlacksmithStationParseException(
    context: CommandContext<*>,
) : ParserException(
    ModdingTableParser::class.java,
    context,
    StandardCaptionKeys.EXCEPTION_INVALID_ARGUMENT
)