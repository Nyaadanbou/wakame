package cc.mewcraft.wakame.command.parser

import cc.mewcraft.wakame.attribute.Attribute
import cc.mewcraft.wakame.attribute.AttributeProvider
import cc.mewcraft.wakame.util.typeTokenOf
import org.incendo.cloud.caption.StandardCaptionKeys
import org.incendo.cloud.component.CommandComponent
import org.incendo.cloud.context.CommandContext
import org.incendo.cloud.context.CommandInput
import org.incendo.cloud.exception.parsing.ParserException
import org.incendo.cloud.parser.*
import org.incendo.cloud.suggestion.BlockingSuggestionProvider
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

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
        val attribute = AttributeParserSupport.attributeProvider.getBy(peekString)
            ?: return ArgumentParseResult.failure(AttributeParseException(commandContext))

        commandInput.readString()

        return ArgumentParseResult.success(attribute)
    }

    override fun stringSuggestions(commandContext: CommandContext<C>, input: CommandInput): Iterable<String> {
        return AttributeParserSupport.attributeProvider.descriptionIds
    }
}

class AttributeParseException(
    context: CommandContext<*>,
) : ParserException(
    AttributeParser::class.java,
    context,
    StandardCaptionKeys.EXCEPTION_INVALID_ARGUMENT
)

private object AttributeParserSupport : KoinComponent {
    val attributeProvider: AttributeProvider by inject()
}