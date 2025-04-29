package cc.mewcraft.wakame.util

import team.unnamed.mocha.MochaEngine
import team.unnamed.mocha.runtime.compiled.MochaCompiledFunction
import java.io.Reader

inline fun <reified B : Any> MochaEngine<*>.bind() {
    bind(B::class.java)
}

inline fun <reified B : Any> MochaEngine<*>.bindInstance(instance: B, name: String, vararg aliases: String) {
    bindInstance(B::class.java, instance, name, *aliases)
}

inline fun <reified F : MochaCompiledFunction> MochaEngine<*>.compileFunc(reader: Reader): F {
    return this.compile(reader, F::class.java)
}

inline fun <reified F : MochaCompiledFunction> MochaEngine<*>.compileFunc(code: String): F {
    return this.compile(code, F::class.java)
}