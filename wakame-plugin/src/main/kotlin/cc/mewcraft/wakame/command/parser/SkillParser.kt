package cc.mewcraft.wakame.command.parser

import cc.mewcraft.wakame.Namespaces
import cc.mewcraft.wakame.registry.SkillRegistry
import cc.mewcraft.wakame.skill2.Skill
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

class SkillParser<C : Any> : AggregateParser<C, Skill> {
    companion object Factory {
        fun <C : Any> skillParser(): ParserDescriptor<C, Skill> {
            return ParserDescriptor.of(SkillParser(), typeTokenOf())
        }

        fun <C : Any> skillComponent(): CommandComponent.Builder<C, Skill> {
            return CommandComponent.builder<C, Skill?>().parser(skillParser())
        }
    }

    override fun valueType(): TypeToken<Skill> {
        return typeTokenOf()
    }

    override fun components(): List<CommandComponent<C>> {
        return listOf(
            SkillPathParser.skillPathComponent<C>().name("path").build()
        )
    }

    override fun mapper(): AggregateResultMapper<C, Skill> {
        return AggregateResultMapper agg@{ commandContext, aggregateCommandContext ->
            val path = aggregateCommandContext.get<String>("path")
            val skill = SkillRegistry.INSTANCES.getOrNull(Key(Namespaces.SKILL, path))
            if (skill == null) {
                ArgumentParseResult.failureFuture(SkillParseException(commandContext))
            } else {
                ArgumentParseResult.successFuture(skill)
            }
        }
    }
}

class SkillParseException(
    context: CommandContext<*>,
) : ParserException(
    SkillParser::class.java, context, StandardCaptionKeys.EXCEPTION_INVALID_ARGUMENT
)