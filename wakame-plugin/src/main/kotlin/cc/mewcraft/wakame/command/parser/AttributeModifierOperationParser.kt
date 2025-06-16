package cc.mewcraft.wakame.command.parser

import cc.mewcraft.wakame.entity.attribute.AttributeModifier
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

class AttributeModifierOperationParser<C : Any> : ArgumentParser<C, AttributeModifier.Operation>, BlockingSuggestionProvider.Strings<C> {
    companion object Factory {
        fun <C : Any> attributeModifierOperationParser(): ParserDescriptor<C, AttributeModifier.Operation> {
            return ParserDescriptor.of(AttributeModifierOperationParser(), typeTokenOf<AttributeModifier.Operation>())
        }

        fun <C : Any> attributeModifierOperationComponent(): CommandComponent.Builder<C, AttributeModifier.Operation> {
            return CommandComponent.builder<C, AttributeModifier.Operation>().parser(attributeModifierOperationParser())
        }
    }

    override fun parse(commandContext: CommandContext<C>, commandInput: CommandInput): ArgumentParseResult<AttributeModifier.Operation> {
        val input = commandInput.peekString()
        val operation = AttributeModifier.Operation.byName(input) ?: return ArgumentParseResult.failure(AttributeModifierOperationParseException(commandContext))

        commandInput.readString()
        return ArgumentParseResult.success(operation)
    }

    override fun stringSuggestions(commandContext: CommandContext<C>, input: CommandInput): Iterable<String> {
        return AttributeModifier.Operation.entries.map { it.key }
    }
}

class AttributeModifierOperationParseException(
    context: CommandContext<*>,
) : ParserException(
    AttributeModifierOperationParser::class.java,
    context,
    StandardCaptionKeys.EXCEPTION_INVALID_ARGUMENT
)