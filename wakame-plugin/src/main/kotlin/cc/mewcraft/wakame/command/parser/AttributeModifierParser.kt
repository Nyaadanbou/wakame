package cc.mewcraft.wakame.command.parser

import cc.mewcraft.wakame.attribute.AttributeModifier
import cc.mewcraft.wakame.util.typeTokenOf
import io.leangen.geantyref.TypeToken
import org.bukkit.NamespacedKey
import org.incendo.cloud.bukkit.parser.NamespacedKeyParser
import org.incendo.cloud.component.CommandComponent
import org.incendo.cloud.parser.ArgumentParseResult
import org.incendo.cloud.parser.ParserDescriptor
import org.incendo.cloud.parser.aggregate.AggregateParser
import org.incendo.cloud.parser.aggregate.AggregateResultMapper
import org.incendo.cloud.parser.standard.DoubleParser

class AttributeModifierParser<C : Any> : AggregateParser<C, AttributeModifier> {
    companion object Factory {
        fun <C : Any> attributeModifierParser(): ParserDescriptor<C, AttributeModifier> {
            return ParserDescriptor.of(AttributeModifierParser(), typeTokenOf())
        }

        fun <C : Any> attributeModifierComponent(): CommandComponent.Builder<C, AttributeModifier> {
            return CommandComponent.builder<C, AttributeModifier>().parser(attributeModifierParser())
        }
    }

    override fun valueType(): TypeToken<AttributeModifier> {
        return typeTokenOf()
    }

    override fun components(): List<CommandComponent<C>> {
        return listOf(
            NamespacedKeyParser.namespacedKeyComponent<C>().required(true).name("id").build(),
            AttributeModifierOperationParser.attributeModifierOperationComponent<C>().required(true).name("operation").build(),
            DoubleParser.doubleComponent<C>().required(true).name("amount").build(),
        )
    }

    override fun mapper(): AggregateResultMapper<C, AttributeModifier> {
        return AggregateResultMapper agg@{ _, aggregateCommandContext ->
            val id = aggregateCommandContext.get<NamespacedKey>("id")
            val operation = aggregateCommandContext.get<AttributeModifier.Operation>("operation")
            val amount = aggregateCommandContext.get<Double>("amount")

            return@agg ArgumentParseResult.successFuture(
                AttributeModifier(
                    id = id,
                    operation = operation,
                    amount = amount
                )
            )
        }
    }
}