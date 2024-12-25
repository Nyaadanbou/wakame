package cc.mewcraft.wakame.command.parser

import cc.mewcraft.wakame.Namespaces
import cc.mewcraft.wakame.registry.AbilityRegistry
import cc.mewcraft.wakame.ability.Ability
import cc.mewcraft.wakame.util.Key
import cc.mewcraft.wakame.util.typeTokenOf
import io.leangen.geantyref.TypeToken
import org.incendo.cloud.caption.StandardCaptionKeys
import org.incendo.cloud.component.CommandComponent
import org.incendo.cloud.context.CommandContext
import org.incendo.cloud.exception.parsing.ParserException
import org.incendo.cloud.parser.ArgumentParseResult
import org.incendo.cloud.parser.ParserDescriptor
import org.incendo.cloud.parser.aggregate.AggregateParser
import org.incendo.cloud.parser.aggregate.AggregateResultMapper

class AbilityParser<C : Any> : AggregateParser<C, Ability> {
    companion object Factory {
        fun <C : Any> abilityParser(): ParserDescriptor<C, Ability> {
            return ParserDescriptor.of(AbilityParser(), typeTokenOf())
        }

        fun <C : Any> abilityComponent(): CommandComponent.Builder<C, Ability> {
            return CommandComponent.builder<C, Ability?>().parser(abilityParser())
        }
    }

    override fun valueType(): TypeToken<Ability> {
        return typeTokenOf()
    }

    override fun components(): List<CommandComponent<C>> {
        return listOf(
            AbilityPathParser.abilityPathComponent<C>().name("path").build()
        )
    }

    override fun mapper(): AggregateResultMapper<C, Ability> {
        return AggregateResultMapper agg@{ commandContext, aggregateCommandContext ->
            val path = aggregateCommandContext.get<String>("path")
            val ability = AbilityRegistry.INSTANCES.getOrNull(Key(Namespaces.ABILITY, path))
            if (ability == null) {
                ArgumentParseResult.failureFuture(AbilityParseException(commandContext))
            } else {
                ArgumentParseResult.successFuture(ability)
            }
        }
    }
}

class AbilityParseException(
    context: CommandContext<*>,
) : ParserException(
    AbilityParser::class.java, context, StandardCaptionKeys.EXCEPTION_INVALID_ARGUMENT
)