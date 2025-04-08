package cc.mewcraft.wakame.command.parser

import cc.mewcraft.wakame.catalog.item.CatalogItemCategory
import cc.mewcraft.wakame.registry2.DynamicRegistries
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

internal class ItemCatalogCategoryParser<C : Any> : ArgumentParser<C, CatalogItemCategory>, BlockingSuggestionProvider.Strings<C> {
    companion object Factory {
        fun <C : Any> categoryParser(): ParserDescriptor<C, CatalogItemCategory> {
            return ParserDescriptor.of(ItemCatalogCategoryParser(), typeTokenOf<CatalogItemCategory>())
        }

        fun <C : Any> categoryComponent(): CommandComponent.Builder<C, CatalogItemCategory> {
            return CommandComponent.builder<C, CatalogItemCategory?>().parser(categoryParser())
        }
    }

    override fun parse(commandContext: CommandContext<C>, commandInput: CommandInput): ArgumentParseResult<CatalogItemCategory> {
        val peekStr = commandInput.peekString()
        if (peekStr !in DynamicRegistries.ITEM_CATEGORY.ids.map(Identifier::value)) {
            return ArgumentParseResult.failure(ItemCatalogCategoryParseException(commandContext))
        }

        val readStr = commandInput.readString()
        return ArgumentParseResult.success(DynamicRegistries.ITEM_CATEGORY[readStr]!!)
    }

    override fun stringSuggestions(commandContext: CommandContext<C>, input: CommandInput): Iterable<String> {
        return DynamicRegistries.ITEM_CATEGORY.ids.map(Identifier::value)
    }
}

class ItemCatalogCategoryParseException(
    context: CommandContext<*>,
) : ParserException(
    ItemCatalogCategoryParser::class.java,
    context,
    StandardCaptionKeys.EXCEPTION_INVALID_ARGUMENT
)