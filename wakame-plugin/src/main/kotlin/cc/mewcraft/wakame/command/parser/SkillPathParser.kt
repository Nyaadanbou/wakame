package cc.mewcraft.wakame.command.parser

import cc.mewcraft.wakame.registry.SkillRegistry
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

class SkillPathParser<C : Any> : ArgumentParser<C, String>, BlockingSuggestionProvider.Strings<C> {
    companion object Factory {
        fun <C : Any> skillPathParser(): ParserDescriptor<C, String> {
            return ParserDescriptor.of(SkillPathParser(), typeTokenOf())
        }

        fun <C : Any> skillPathComponent(): CommandComponent.Builder<C, String> {
            return CommandComponent.builder<C, String>().parser(skillPathParser())
        }
    }

    override fun parse(commandContext: CommandContext<C>, commandInput: CommandInput): ArgumentParseResult<String> {
        val paths = SkillRegistry.PATHS

        val peekString = commandInput.peekString()
        if (peekString !in paths) {
            return ArgumentParseResult.failure(SkillPathParseException(commandContext))
        }

        val readString = commandInput.readString()
        return ArgumentParseResult.success(readString)
    }

    override fun stringSuggestions(commandContext: CommandContext<C>, input: CommandInput): Iterable<String> {
        return SkillRegistry.PATHS
    }
}

class SkillPathParseException(
    context: CommandContext<*>,
) : ParserException(
    SkillPathParser::class.java,
    context,
    StandardCaptionKeys.EXCEPTION_INVALID_ARGUMENT
)