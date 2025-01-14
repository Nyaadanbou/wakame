package cc.mewcraft.wakame.molang

import cc.mewcraft.wakame.config.configurate.TypeSerializer
import me.lucko.helper.function.Numbers
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.ConfigurationOptions
import org.spongepowered.configurate.kotlin.extensions.get
import team.unnamed.mocha.MochaEngine
import java.lang.reflect.Type
import kotlin.jvm.optionals.getOrNull

/**
 * 表示一个可以被 [MochaEngine] 解析的东西.
 *
 * @param T 该 [Evaluable] 由类型 [T] 的值组成.
 */
// FIXME 重命名为 Expression(表达式), 去除泛型
interface Evaluable<T : Any> {
    companion object {
        /**
         * 从一个字符串创建一个 [Evaluable].
         */
        fun parseExpression(value: String): Evaluable<String> = StringEval(value)

        /**
         * 从一个数字创建一个 [Evaluable].
         */
        fun parseNumber(value: Number): Evaluable<Number> = NumberEval(value)
    }

    fun asString(): String

    fun evaluate(engine: MochaEngine<*>): Double

    fun evaluate(): Double
}

internal object EvaluableSerializer : TypeSerializer<Evaluable<*>> {
    override fun deserialize(type: Type, node: ConfigurationNode): Evaluable<*> {
        val string = node.get<String>()
        val evalNumber = string?.let { Numbers.parse(it).getOrNull() }
        if (evalNumber != null)
            return Evaluable.parseNumber(evalNumber)

        val evalString = string?.let { Evaluable.parseExpression(it) }
        return evalString ?: throw IllegalArgumentException("Cannot deserialize Evaluable from ${node.path()}")
    }

    override fun emptyValue(specificType: Type, options: ConfigurationOptions): Evaluable<*> {
        return Evaluable.parseNumber(0)
    }
}

/**
 * 表示一个字符串 MoLang 表达式.
 */
private data class StringEval(val value: String) : Evaluable<String> {
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
private data class NumberEval(val value: Number) : Evaluable<Number> {
    override fun evaluate(engine: MochaEngine<*>): Double = value.toDouble()
    override fun evaluate(): Double = value.toDouble()
    override fun asString(): String {
        return value.toString()
    }
}