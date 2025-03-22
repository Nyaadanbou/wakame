package cc.mewcraft.wakame.molang

import cc.mewcraft.wakame.config.configurate.TypeSerializer
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.ConfigurationOptions
import org.spongepowered.configurate.kotlin.extensions.get
import team.unnamed.mocha.MochaEngine
import java.lang.reflect.Type

/**
 * 表示一个可以被 [MochaEngine] 解析的东西.
 */
interface Expression {
    companion object {
        /**
         * 从一个字符串创建一个 [Expression].
         */
        fun of(value: String): Expression = StringEval(value)

        /**
         * 从一个数字创建一个 [Expression].
         */
        fun of(value: Number): Expression = NumberEval(value)
    }

    fun asString(): String

    fun evaluate(engine: MochaEngine<*>): Double

    fun evaluate(): Double
}

internal object ExpressionSerializer : TypeSerializer<Expression> {
    override fun deserialize(type: Type, node: ConfigurationNode): Expression {
        val string = node.get<String>()
        val evalNumber = string?.toDoubleOrNull()
        if (evalNumber != null)
            return Expression.of(evalNumber)

        val evalString = string?.let { Expression.of(it) }
        return evalString ?: throw IllegalArgumentException("Cannot deserialize Expression from ${node.path()}")
    }

    override fun emptyValue(specificType: Type, options: ConfigurationOptions): Expression {
        return Expression.of(0)
    }
}

/**
 * 表示一个字符串 MoLang 表达式.
 */
private data class StringEval(val value: String) : Expression {
    override fun evaluate(engine: MochaEngine<*>): Double = engine.eval(value)
    override fun evaluate(): Double {
        val engine = MoLangSupport.createEngine()
        return evaluate(engine)
    }

    override fun asString(): String {
        return value
    }
}

/**
 * 表示一个数字.
 */
private data class NumberEval(val value: Number) : Expression {
    override fun evaluate(engine: MochaEngine<*>): Double = value.toDouble()
    override fun evaluate(): Double = value.toDouble()
    override fun asString(): String {
        return value.toString()
    }
}