package cc.mewcraft.wakame.command.parser

import cc.mewcraft.wakame.ability2.meta.AbilityMeta
import cc.mewcraft.wakame.registry2.BuiltInRegistries
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

class AbilityMetaParser<C : Any> : ArgumentParser<C, AbilityMeta>, BlockingSuggestionProvider.Strings<C> {
    companion object Factory {
        fun <C : Any> abilityMetaParser(): ParserDescriptor<C, AbilityMeta> {
            return ParserDescriptor.of(AbilityMetaParser(), typeTokenOf())
        }

        fun <C : Any> abilityMetaComponent(): CommandComponent.Builder<C, AbilityMeta> {
            return CommandComponent.builder<C, AbilityMeta>().parser(abilityMetaParser())
        }
    }

    override fun parse(commandContext: CommandContext<C>, commandInput: CommandInput): ArgumentParseResult<AbilityMeta> {
        val peekString = commandInput.peekString()
        val ability = BuiltInRegistries.ABILITY_META[peekString]
        if (ability == null) {
            return ArgumentParseResult.failure(AbilityParseException(commandContext))
        }

        commandInput.readString()
        return ArgumentParseResult.success(ability)
    }

    override fun stringSuggestions(commandContext: CommandContext<C>, input: CommandInput): Iterable<String> {
        return BuiltInRegistries.ABILITY_META.keys.map { it.value.value() }
    }
}

class AbilityParseException(
    context: CommandContext<*>,
) : ParserException(
    AbilityMetaParser::class.java, context, StandardCaptionKeys.EXCEPTION_INVALID_ARGUMENT
)