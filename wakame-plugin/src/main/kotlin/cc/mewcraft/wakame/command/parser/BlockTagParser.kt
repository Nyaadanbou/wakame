package cc.mewcraft.wakame.command.parser

import cc.mewcraft.wakame.util.typeTokenOf
import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import io.papermc.paper.registry.tag.Tag
import io.papermc.paper.registry.tag.TagKey
import org.bukkit.block.BlockType
import org.incendo.cloud.caption.StandardCaptionKeys
import org.incendo.cloud.component.CommandComponent
import org.incendo.cloud.context.CommandContext
import org.incendo.cloud.context.CommandInput
import org.incendo.cloud.exception.parsing.ParserException
import org.incendo.cloud.parser.ArgumentParseResult
import org.incendo.cloud.parser.ArgumentParser
import org.incendo.cloud.parser.ParserDescriptor
import org.incendo.cloud.suggestion.BlockingSuggestionProvider

internal class BlockTagParser<C : Any> : ArgumentParser<C, Tag<BlockType>>, BlockingSuggestionProvider.Strings<C> {
    companion object Factory {
        fun <C : Any> blockTagParser(): ParserDescriptor<C, Tag<BlockType>> {
            return ParserDescriptor.of(BlockTagParser(), typeTokenOf<Tag<BlockType>>())
        }

        fun <C : Any> blockTagComponent(): CommandComponent.Builder<C, Tag<BlockType>> {
            return CommandComponent.builder<C, Tag<BlockType>>().parser(blockTagParser())
        }
    }

    override fun parse(commandContext: CommandContext<C>, commandInput: CommandInput): ArgumentParseResult<Tag<BlockType>> {
        val peekStr = commandInput.peekString()
        val registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.BLOCK)
        val tagKey = TagKey.create(RegistryKey.BLOCK, peekStr)
        if (!registry.hasTag(tagKey)) {
            return ArgumentParseResult.failure(BlockTagParseException(commandContext))
        }

        commandInput.readString()
        return ArgumentParseResult.success(registry.getTag(tagKey))
    }

    override fun stringSuggestions(commandContext: CommandContext<C>, input: CommandInput): Iterable<String> {
        val registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.BLOCK)
        return registry.tags.map { it.tagKey().key().toString() }
    }
}

class BlockTagParseException(
    context: CommandContext<*>,
) : ParserException(
    BlockTagParser::class.java,
    context,
    StandardCaptionKeys.EXCEPTION_INVALID_ARGUMENT
)