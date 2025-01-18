package cc.mewcraft.wakame.command.parser

import cc.mewcraft.wakame.registry2.KoishRegistries
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

class AbilityPathParser<C : Any> : ArgumentParser<C, String>, BlockingSuggestionProvider.Strings<C> {
    companion object Factory {
        fun <C : Any> abilityPathParser(): ParserDescriptor<C, String> {
            return ParserDescriptor.of(AbilityPathParser(), typeTokenOf())
        }

        fun <C : Any> abilityPathComponent(): CommandComponent.Builder<C, String> {
            return CommandComponent.builder<C, String>().parser(abilityPathParser())
        }
    }

    override fun parse(commandContext: CommandContext<C>, commandInput: CommandInput): ArgumentParseResult<String> {
        val paths = KoishRegistries.ABILITY.keys.map { it.value.value() }

        val peekString = commandInput.peekString()
        if (peekString !in paths) {
            return ArgumentParseResult.failure(AbilityPathParseException(commandContext))
        }

        val readString = commandInput.readString()
        return ArgumentParseResult.success(readString)
    }

    override fun stringSuggestions(commandContext: CommandContext<C>, input: CommandInput): Iterable<String> {
        return KoishRegistries.ABILITY.keys.map { it.value.value() }
    }
}

class AbilityPathParseException(
    context: CommandContext<*>,
) : ParserException(
    AbilityPathParser::class.java,
    context,
    StandardCaptionKeys.EXCEPTION_INVALID_ARGUMENT
)