package cc.mewcraft.wakame.command.parser

import cc.mewcraft.wakame.reforge.blacksmith.BlacksmithStation
import cc.mewcraft.wakame.reforge.blacksmith.WtfBlacksmithStation
import cc.mewcraft.wakame.util.typeTokenOf
import org.incendo.cloud.caption.StandardCaptionKeys
import org.incendo.cloud.component.CommandComponent
import org.incendo.cloud.context.CommandContext
import org.incendo.cloud.context.CommandInput
import org.incendo.cloud.exception.parsing.ParserException
import org.incendo.cloud.parser.*
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
        // val peekStr = commandInput.peekString()
        // if (peekStr !in BlacksmithStationRegistry.NAMES) {
        //     return ArgumentParseResult.failure(BlacksmithStationParseException(commandContext))
        // }
        //
        // val readStr = commandInput.readString()
        // return ArgumentParseResult.success(BlacksmithStationRegistry[readStr]!!)
        // TODO #227 从 BlacksmithStationRegistry 读取 BlacksmithStation 实例
        val readStr = commandInput.readString()
        return ArgumentParseResult.success(WtfBlacksmithStation)
    }

    override fun stringSuggestions(commandContext: CommandContext<C>, input: CommandInput): Iterable<String> {
        // return BlacksmithStationRegistry.NAMES
        // TODO #227 从 BlacksmithStationRegistry 读取所有的 BlacksmithStation id
        return listOf("wtf")
    }
}

class BlacksmithStationParseException(
    context: CommandContext<*>,
) : ParserException(
    ModdingTableParser::class.java,
    context,
    StandardCaptionKeys.EXCEPTION_INVALID_ARGUMENT
)