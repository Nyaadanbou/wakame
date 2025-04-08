package cc.mewcraft.wakame.command.parser

import cc.mewcraft.wakame.entity.attribute.Attribute
import cc.mewcraft.wakame.entity.attribute.Attributes
import cc.mewcraft.wakame.registry2.BuiltInRegistries
import cc.mewcraft.wakame.util.Identifier
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

class AttributeParser<C : Any> : ArgumentParser<C, Attribute>, BlockingSuggestionProvider.Strings<C> {
    companion object Factory {
        fun <C : Any> attributeParser(): ParserDescriptor<C, Attribute> {
            return ParserDescriptor.of(AttributeParser(), typeTokenOf<Attribute>())
        }

        fun <C : Any> attributeComponent(): CommandComponent.Builder<C, Attribute> {
            return CommandComponent.builder<C, Attribute>().parser(attributeParser())
        }
    }

    override fun parse(commandContext: CommandContext<C>, commandInput: CommandInput): ArgumentParseResult<Attribute> {
        val peekString = commandInput.peekString()
        val attribute = Attributes.get(peekString)
            ?: return ArgumentParseResult.failure(AttributeParseException(commandContext))

        commandInput.readString()

        return ArgumentParseResult.success(attribute)
    }

    override fun stringSuggestions(commandContext: CommandContext<C>, input: CommandInput): Iterable<String> {
        return BuiltInRegistries.ATTRIBUTE.ids.map(Identifier::value)
    }
}

class AttributeParseException(
    context: CommandContext<*>,
) : ParserException(
    AttributeParser::class.java,
    context,
    StandardCaptionKeys.EXCEPTION_INVALID_ARGUMENT
)
