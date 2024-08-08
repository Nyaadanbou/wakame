package cc.mewcraft.wakame.command.parser

import cc.mewcraft.wakame.reforge.reroll.RerollingTable
import cc.mewcraft.wakame.reforge.reroll.RerollingTables
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

class RerollingTableParser<C : Any> : ArgumentParser<C, RerollingTable>, BlockingSuggestionProvider.Strings<C> {
    companion object Factory {
        fun <C : Any> rerollingTableParser(): ParserDescriptor<C, RerollingTable> {
            return ParserDescriptor.of(RerollingTableParser(), typeTokenOf<RerollingTable>())
        }

        fun <C : Any> rerollingTableComponent(): CommandComponent.Builder<C, RerollingTable> {
            return CommandComponent.builder<C, RerollingTable?>().parser(rerollingTableParser())
        }
    }

    override fun parse(commandContext: CommandContext<C>, commandInput: CommandInput): ArgumentParseResult<RerollingTable> {
        val peekStr = commandInput.peekString()
        if (peekStr !in RerollingTables.NAMES) {
            return ArgumentParseResult.failure(RerollingTableParseException(commandContext))
        }

        val readStr = commandInput.readString()
        return ArgumentParseResult.success(RerollingTables[readStr]!!)
    }

    override fun stringSuggestions(commandContext: CommandContext<C>, input: CommandInput): Iterable<String> {
        return RerollingTables.NAMES
    }
}

class RerollingTableParseException(
    context: CommandContext<*>,
) : ParserException(
    ModdingTableParser::class.java,
    context,
    StandardCaptionKeys.EXCEPTION_INVALID_ARGUMENT
)