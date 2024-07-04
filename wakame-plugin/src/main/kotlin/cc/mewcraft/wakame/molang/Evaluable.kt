package cc.mewcraft.wakame.molang

import cc.mewcraft.wakame.SchemaSerializer
import me.lucko.helper.function.Numbers
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.extensions.get
import team.unnamed.mocha.MochaEngine
import java.lang.reflect.Type
import kotlin.jvm.optionals.getOrNull

/**
 * 表示一个可以被 [MochaEngine] 解析的东西.
 *
 * @param T 该 [Evaluable] 由类型 [T] 的值组成.
 */
fun interface Evaluable<T : Any> {
    /**
     * 表示一个字符串 MoLang 表达式。
     */
    data class StringEval(val value: String) : Evaluable<String> {
        override fun evaluate(engine: MochaEngine<*>): Double = engine.eval(value)
    }

    /**
     * 表示一个数字。
     */
    data class NumberEval(val value: Number) : Evaluable<Number> {
        override fun evaluate(engine: MochaEngine<*>): Double = value.toDouble()
    }

    fun evaluate(engine: MochaEngine<*>) : Double

    fun evaluate(): Double {
        val engine = MoLangSupport.createEngine()
        return evaluate(engine)
    }
}

internal object EvaluableSerializer : SchemaSerializer<Evaluable<*>> {
    override fun deserialize(type: Type, node: ConfigurationNode): Evaluable<*> {
        val string = node.get<String>()
        val evalNumber = string?.let { Numbers.parse(it).getOrNull() }
        if (evalNumber != null)
            return Evaluable.NumberEval(evalNumber)

        val evalString = string?.let { Evaluable.StringEval(it) }
        return evalString ?: throw IllegalArgumentException("Cannot deserialize Evaluable from ${node.path()}")
    }
}