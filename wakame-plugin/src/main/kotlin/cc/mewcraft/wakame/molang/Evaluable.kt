package cc.mewcraft.wakame.molang

import cc.mewcraft.wakame.SchemaSerializer
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.extensions.get
import team.unnamed.mocha.MochaEngine
import java.io.Reader
import java.lang.reflect.Type
import kotlin.io.path.Path
import kotlin.io.path.bufferedReader

/**
 * 表示一个可以被 [MochaEngine] 解析的东西
 */
fun interface Evaluable<T : Any> {
    /**
     * 表示一个字符串 MoLang 表达式。
     */
    data class StringEval(val value: String) : Evaluable<String> {
        override fun evaluate(engine: MochaEngine<*>): Double = engine.eval(value)
    }

    /**
     * 表示可以从一个 [Reader] 获取到的东西，此东西应该能被解析。
     */
    data class ReaderEval(val value: Reader) : Evaluable<Reader> {
        override fun evaluate(engine: MochaEngine<*>): Double = engine.eval(value)
    }

    fun evaluate(engine: MochaEngine<*>) : Double
}

internal object EvaluableSerializer : SchemaSerializer<Evaluable<*>>, KoinComponent {
    private val engine: MochaEngine<*> by inject()

    override fun deserialize(type: Type, node: ConfigurationNode): Evaluable<*> {
        val evalString = (node.string ?: node.node("eval").get<String>())?.let { Evaluable.StringEval(it) }
        val evalReader = node.node("path").get<String>()?.let { Evaluable.ReaderEval(Path(it).bufferedReader()) }

        val isExist = (evalString == null) xor (evalReader == null)
        require(isExist) { "Either 'eval' or 'path' must be specified" }
        val evaluable = evalString ?: evalReader!!
        evaluable.evaluate(engine)

        return evaluable
    }
}