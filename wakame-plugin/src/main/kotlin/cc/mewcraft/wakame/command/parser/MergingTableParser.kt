package cc.mewcraft.wakame.command.parser

import cc.mewcraft.wakame.reforge.merge.MergingTable
import cc.mewcraft.wakame.reforge.merge.MergingTableRegistry
import cc.mewcraft.wakame.util.typeTokenOf
import org.incendo.cloud.caption.StandardCaptionKeys
import org.incendo.cloud.component.CommandComponent
import org.incendo.cloud.context.CommandContext
import org.incendo.cloud.context.CommandInput
import org.incendo.cloud.exception.parsing.ParserException
import org.incendo.cloud.parser.*
import org.incendo.cloud.suggestion.BlockingSuggestionProvider

class MergingTableParser<C : Any> : ArgumentParser<C, MergingTable>, BlockingSuggestionProvider.Strings<C> {
    companion object Factory {
        fun <C : Any> mergingTableParser(): ParserDescriptor<C, MergingTable> {
            return ParserDescriptor.of(MergingTableParser(), typeTokenOf<MergingTable>())
        }

        fun <C : Any> mergingTableComponent(): CommandComponent.Builder<C, MergingTable> {
            return CommandComponent.builder<C, MergingTable?>().parser(mergingTableParser())
        }
    }

    override fun parse(commandContext: CommandContext<C>, commandInput: CommandInput): ArgumentParseResult<MergingTable> {
        val peekStr = commandInput.peekString()
        if (peekStr !in MergingTableRegistry.NAMES) {
            return ArgumentParseResult.failure(MergingTableParseException(commandContext))
        }

        val readStr = commandInput.readString()
        return ArgumentParseResult.success(MergingTableRegistry[readStr]!!)
    }

    override fun stringSuggestions(commandContext: CommandContext<C>, input: CommandInput): Iterable<String> {
        return MergingTableRegistry.NAMES
    }
}

class MergingTableParseException(
    context: CommandContext<*>,
) : ParserException(
    ModdingTableParser::class.java,
    context,
    StandardCaptionKeys.EXCEPTION_INVALID_ARGUMENT
)