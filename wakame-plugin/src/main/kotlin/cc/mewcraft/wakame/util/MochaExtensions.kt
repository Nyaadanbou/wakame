package cc.mewcraft.wakame.util

import team.unnamed.mocha.MochaEngine

inline fun <reified B : Any> MochaEngine<*>.bind() {
    bind(B::class.java)
}

inline fun <reified B : Any> MochaEngine<*>.bindInstance(instance: B, name: String, vararg aliases: String) {
    bindInstance(B::class.java, instance, name, *aliases)
}