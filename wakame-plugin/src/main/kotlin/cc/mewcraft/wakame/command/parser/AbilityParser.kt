package cc.mewcraft.wakame.command.parser

import cc.mewcraft.wakame.ability.Ability
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

class AbilityParser<C : Any> : ArgumentParser<C, Ability>, BlockingSuggestionProvider.Strings<C> {
    companion object Factory {
        fun <C : Any> abilityParser(): ParserDescriptor<C, Ability> {
            return ParserDescriptor.of(AbilityParser(), typeTokenOf())
        }

        fun <C : Any> abilityComponent(): CommandComponent.Builder<C, Ability> {
            return CommandComponent.builder<C, Ability>().parser(abilityParser())
        }
    }

    override fun parse(commandContext: CommandContext<C>, commandInput: CommandInput): ArgumentParseResult<Ability> {
        val peekString = commandInput.peekString()
        val ability = KoishRegistries.ABILITY[peekString]
        if (ability == null) {
            return ArgumentParseResult.failure(AbilityParseException(commandContext))
        }

        commandInput.readString()
        return ArgumentParseResult.success(ability)
    }

    override fun stringSuggestions(commandContext: CommandContext<C>, input: CommandInput): Iterable<String> {
        return KoishRegistries.ABILITY.keys.map { it.value.value() }
    }
}

class AbilityParseException(
    context: CommandContext<*>,
) : ParserException(
    AbilityParser::class.java, context, StandardCaptionKeys.EXCEPTION_INVALID_ARGUMENT
)