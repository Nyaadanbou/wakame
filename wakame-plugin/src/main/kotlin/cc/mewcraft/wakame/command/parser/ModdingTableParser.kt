package cc.mewcraft.wakame.command.parser

import cc.mewcraft.wakame.reforge.mod.ModdingTable
import cc.mewcraft.wakame.reforge.mod.ModdingTableRegistry
import cc.mewcraft.wakame.util.typeTokenOf
import org.incendo.cloud.caption.StandardCaptionKeys
import org.incendo.cloud.component.CommandComponent
import org.incendo.cloud.context.CommandContext
import org.incendo.cloud.context.CommandInput
import org.incendo.cloud.exception.parsing.ParserException
import org.incendo.cloud.parser.*
import org.incendo.cloud.suggestion.BlockingSuggestionProvider

class ModdingTableParser<C : Any> : ArgumentParser<C, ModdingTable>, BlockingSuggestionProvider.Strings<C> {
    companion object Factory {
        fun <C : Any> moddingTableParser(): ParserDescriptor<C, ModdingTable> {
            return ParserDescriptor.of(ModdingTableParser(), typeTokenOf<ModdingTable>())
        }

        fun <C : Any> moddingTableComponent(): CommandComponent.Builder<C, ModdingTable> {
            return CommandComponent.builder<C, ModdingTable?>().parser(moddingTableParser())
        }
    }

    override fun parse(commandContext: CommandContext<C>, commandInput: CommandInput): ArgumentParseResult<ModdingTable> {
        val peekStr = commandInput.peekString()
        if (peekStr !in ModdingTableRegistry.NAMES) {
            return ArgumentParseResult.failure(ModdingTableParseException(commandContext))
        }

        val readStr = commandInput.readString()
        return ArgumentParseResult.success(ModdingTableRegistry[readStr]!!)
    }

    override fun stringSuggestions(commandContext: CommandContext<C>, input: CommandInput): Iterable<String> {
        return ModdingTableRegistry.NAMES
    }
}

class ModdingTableParseException(
    context: CommandContext<*>,
) : ParserException(
    ModdingTableParser::class.java,
    context,
    StandardCaptionKeys.EXCEPTION_INVALID_ARGUMENT
)